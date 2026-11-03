package ru.he3hauka.toolvisuals.relay.paper;

import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import ru.he3hauka.toolvisuals.animation.network.*;
import ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire;
import ru.he3hauka.toolvisuals.animation.protocol.server.*;
import ru.he3hauka.toolvisuals.animation.protocol.social.SocialCommandWire;
import ru.he3hauka.toolvisuals.animation.social.*;
import ru.he3hauka.toolvisuals.cosmetic.cape.*;
import ru.he3hauka.toolvisuals.relay.paper.cape.CapeRelay;
import ru.he3hauka.toolvisuals.relay.paper.accessory.AccessoryRelay;
import ru.he3hauka.toolvisuals.cosmetic.accessory.*;
import ru.he3hauka.toolvisuals.relay.paper.presence.PresenceRelay;
import ru.he3hauka.toolvisuals.network.presence.PresenceWire;
import ru.he3hauka.toolvisuals.network.ingress.RequestAdmission;
import ru.he3hauka.toolvisuals.relay.paper.presence.ClientStatus;
import ru.he3hauka.toolvisuals.relay.paper.reward.RewardService;
import ru.he3hauka.toolvisuals.relay.paper.reward.RewardCommand;
import ru.he3hauka.toolvisuals.relay.paper.config.ServerConfiguration;
import static ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire.*;

public final class ToolVisualsEmotesPlugin extends JavaPlugin implements Listener, PluginMessageListener {
    private record SocialNotice(UUID source,SocialProtocol.State state) {}
    private static final class Peer {
        final Player player;
        final UUID id;
        final PlaybackLedger.Handle handle;
        final PacketBudget requests;
        final PacketBudget socialBudget;
        final CommandInbox incoming=new CommandInbox();
        final RelayMailbox<SocialProtocol.Request> social=new RelayMailbox<>(4);
        final RelayMailbox<SocialCommandWire.Request> socialCommands=new RelayMailbox<>(1);
        SocialCommandWire.Reply socialReply;
        final StateOutbox outgoing=new StateOutbox(256);
        final CapeRelay.Endpoint cape;
        final AccessoryRelay.Endpoint accessory;
        final PresenceRelay.Endpoint presence;
        final LinkedHashMap<UUID,SocialNotice> notices=new LinkedHashMap<>();
        final LinkedHashMap<Integer,Reply> replies=new LinkedHashMap<>();
        final Set<UUID> publishedBadges=new HashSet<>();
        final AtomicBoolean scheduled=new AtomicBoolean();
        final AtomicBoolean refreshing=new AtomicBoolean();
        final AtomicBoolean closed=new AtomicBoolean();
        volatile Set<String> channels=Set.of();
        volatile Set<UUID> viewers=Set.of();
        volatile UUID world;
        final ru.he3hauka.toolvisuals.relay.paper.world.WorldContextDelivery worldContext = new ru.he3hauka.toolvisuals.relay.paper.world.WorldContextDelivery();
        volatile ScheduledTask watch;
        long legacySequence,legacyPlayback,ticks;
        Peer(Player player,PlaybackLedger.Handle handle,Peer previous,UUID server){
            this.player=player;this.id=handle.actor();this.handle=handle;
            cape=new CapeRelay.Endpoint(previous==null?null:previous.cape);
            accessory=new AccessoryRelay.Endpoint(previous==null?null:previous.accessory);
            presence=new PresenceRelay.Endpoint(server,handle.epoch(),previous==null?null:previous.presence);
            requests=previous==null?new PacketBudget(12,8,System.nanoTime()):previous.requests;
            socialBudget=previous==null?new PacketBudget(8,4,System.nanoTime()):previous.socialBudget;
            if(previous!=null){legacySequence=previous.legacySequence;legacyPlayback=previous.legacyPlayback;synchronized(previous){publishedBadges.addAll(previous.publishedBadges);}}
        }
        synchronized boolean accept(boolean social,long now){return !closed.get()&&(social?socialBudget:requests).allow(now);}
        synchronized void reply(Reply value){
            int key=value.operation()==Operation.HELLO?0:1;
            var old=replies.get(key);
            if(old==null||value.requestId()>=old.requestId())replies.put(key,value);
        }
        synchronized void notice(UUID source,SocialProtocol.State value){
            UUID key=SocialProtocol.key(value);
            if(!notices.containsKey(key)&&notices.size()>=64&&!(value instanceof SocialProtocol.Badge)){
                var iterator=notices.entrySet().iterator();while(iterator.hasNext())if(iterator.next().getValue().state() instanceof SocialProtocol.Badge){iterator.remove();break;}
            }
            if(notices.containsKey(key)||notices.size()<64)notices.put(key,new SocialNotice(source,value));
        }
        synchronized void socialReply(SocialCommandWire.Reply reply){if(socialReply==null||reply.id()>=socialReply.id())socialReply=reply;}
        synchronized boolean permitsBadge(UUID actor,boolean visible){return publishedBadges.contains(actor)||visible&&publishedBadges.size()<RelayLimits.MAX_PLAYERS;}
        synchronized void sentBadge(UUID actor,boolean visible){if(visible)publishedBadges.add(actor);else publishedBadges.remove(actor);}
        synchronized Object control(){if(socialReply!=null)return socialReply;if(cape.acknowledgement()!=null)return cape.acknowledgement();if(accessory.acknowledgement()!=null)return accessory.acknowledgement();if(!replies.isEmpty())return replies.values().iterator().next();return notices.isEmpty()?null:notices.values().iterator().next();}
        synchronized void sent(Object value){if(value instanceof SocialCommandWire.Reply){if(socialReply==value)socialReply=null;}else if(value instanceof AccessoryWire.Ack ack)accessory.sent(ack);else if(value instanceof CapeWire.Ack ack)cape.sent(ack);else if(value instanceof Reply reply)replies.values().removeIf(v->v==reply);else notices.values().removeIf(v->v==value);}
        synchronized boolean pending(){return socialReply!=null||!replies.isEmpty()||!notices.isEmpty()||outgoing.next()!=null||cape.pending()||accessory.pending();}
    }

    private final Map<UUID,Peer> peers=new ConcurrentHashMap<>();
    private final PlaybackLedger ledger=new PlaybackLedger();
    private final CapeRelay capes=new CapeRelay(ledger.server);
    private final AccessoryRelay accessories=new AccessoryRelay(ledger.server);
    private final PresenceRelay presence=new PresenceRelay();
    private final RelayLimits limits=new RelayLimits(System.nanoTime());
    private final RequestAdmission ingress=new RequestAdmission(System.nanoTime());
    private final ru.he3hauka.toolvisuals.animation.protocol.server.ObserverAdmissions observers=new ru.he3hauka.toolvisuals.animation.protocol.server.ObserverAdmissions();
    private final LongAdder malformed=new LongAdder(),throttled=new LongAdder(),deferred=new LongAdder(),sent=new LongAdder();
    private ScheduledTask cleanup;
    private volatile boolean running;
    private final ClientStatus clients = new ClientStatus();
    private RewardService rewards;
    private AutoCloseable placeholders;
    private ru.he3hauka.toolvisuals.relay.paper.minigame.MiniGameRelay minigames;
    private ru.he3hauka.toolvisuals.relay.paper.friends.FriendRelay friendRelay;

    @Override public void onEnable(){
        running=true;
        minigames=new ru.he3hauka.toolvisuals.relay.paper.minigame.MiniGameRelay(this);
        friendRelay=new ru.he3hauka.toolvisuals.relay.paper.friends.FriendRelay(this,ledger.server);
        ServerConfiguration configuration = null;
        try { configuration = new ServerConfiguration(this); rewards = new RewardService(this, clients, configuration); }
        catch (Exception | LinkageError error) { getLogger().log(java.util.logging.Level.SEVERE, "Rewards could not start; the visual relay remains available", error); }
        var command = Objects.requireNonNull(getCommand("toolvisuals"));
        var rewardCommand = new RewardCommand(rewards, configuration == null ? null : configuration.reward); command.setExecutor(rewardCommand); command.setTabCompleter(rewardCommand);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholders = ru.he3hauka.toolvisuals.relay.paper.placeholder.PlaceholderHook.register(this, clients,
                    configuration == null ? "Да" : configuration.reward.usingText, configuration == null ? "Нет" : configuration.reward.notUsingText);
        }
        var messenger=getServer().getMessenger();
        for(var channel:RequestAdmission.Channel.values())messenger.registerIncomingPluginChannel(this,channel.id,this);
        for(String channel:OUTPUT_CHANNELS)messenger.registerOutgoingPluginChannel(this,channel);
        getServer().getPluginManager().registerEvents(this,this);
        cleanup=getServer().getGlobalRegionScheduler().runAtFixedRate(this,task->route(ledger.expireInvitations(System.nanoTime())),5,5);
        getLogger().info("Playback relay v3 enabled; bounded acknowledged sessions, legacy v1/v2 supported");
    }
    @Override public void onDisable(){
        running=false;
        if(minigames!=null)minigames.close();
        if(friendRelay!=null)friendRelay.close();
        ingress.clear();
        if(cleanup!=null)cleanup.cancel();
        for(var peer:List.copyOf(peers.values()))retire(peer);
        if (rewards != null) rewards.close();
        if (placeholders != null) try { placeholders.close(); } catch (Exception error) { getLogger().warning("Could not unregister ToolVisuals placeholders"); }
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getLogger().info("Relay packets="+sent.sum()+", malformed="+malformed.sum()+", throttled="+throttled.sum()+", deferred="+deferred.sum());
    }
    private Peer attach(Player player){return attach(player,null);}
    private synchronized Peer attach(Player player,Peer previous){
        UUID id=player.getUniqueId();var old=peers.get(id);
        if(old!=null)return old.player==player&&!old.closed.get()?old:null;
        var opened=ledger.open(id);if(opened==null)return null;
        var peer=new Peer(player,opened.handle(),previous,ledger.server);peers.put(id,peer);route(opened.effects());
        clients.open(id, peer.handle.epoch());
        if (rewards != null) rewards.open(id, peer.handle.epoch(), player.getName());
        peer.watch=player.getScheduler().runAtFixedRate(this,task->watch(peer),()->retire(peer),1,2);
        if(peer.watch==null){retire(peer);return null;}return peer;
    }
    private void retire(Peer peer){
        if(!peer.closed.compareAndSet(false,true))return;
        clients.close(peer.id, peer.handle.epoch());
        if (rewards != null) rewards.disconnect(peer.id, peer.handle.epoch());
        route(ledger.close(peer.handle));
        broadcastCape(peer,capes.remove(peer.id,peer.handle.epoch()));
        broadcastAccessory(peer,accessories.remove(peer.id,peer.handle.epoch()));peer.accessory.close();
        peer.cape.close();peer.presence.close();presence.remove(peer.id,peer.handle.epoch());
        peers.remove(peer.id,peer);limits.remove(peer.id);
        peer.incoming.close();peer.social.close();peer.socialCommands.close();peer.outgoing.close();
        synchronized(peer){peer.notices.clear();peer.replies.clear();peer.socialReply=null;}
        if(peer.watch!=null)peer.watch.cancel();
    }
    private boolean current(Peer peer){return running&&!peer.closed.get()&&peers.get(peer.id)==peer;}
    private static final Set<String> OUTPUT_CHANNELS=Set.of(ru.he3hauka.toolvisuals.network.friends.FriendWire.STATE_CHANNEL,ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.STATE_CHANNEL,STATE_CHANNEL,REPLY_CHANNEL,SocialProtocol.STATE_CHANNEL,EmoteWire.STATE_CHANNEL,EmoteWire.EXTENDED_STATE_CHANNEL,CapeWire.STATE_CHANNEL,CapeWire.ANIMATED_STATE_CHANNEL,CapeTransferWire.STATE_CHANNEL,CapeWire.ACK_CHANNEL,PresenceWire.STATE_CHANNEL,AccessoryWire.STATE_CHANNEL,AccessoryWire.EXPANDED_STATE_CHANNEL,AccessoryWire.ACK_CHANNEL,SocialCommandWire.REPLY_CHANNEL,ru.he3hauka.toolvisuals.network.world.WorldContextWire.CHANNEL,ru.he3hauka.toolvisuals.network.world.WorldContextWire.EXTENDED_CHANNEL,ru.he3hauka.toolvisuals.network.world.WorldContextWire.NAMED_CHANNEL);
    private void capabilities(Peer peer){
        peer.channels=capabilities(peer.player);
    }
    private Set<String> capabilities(Player player){
        var listening=player.getListeningPluginChannels();var supported=new HashSet<String>();
        for(var channel:OUTPUT_CHANNELS)if(listening.contains(channel))supported.add(channel);
        return Set.copyOf(supported);
    }
    private void discover(Player player){discover(player,null);}
    private void discover(Player player,Peer source){
        UUID id=player.getUniqueId();if(peers.containsKey(id)||!observers.acquire(id,System.nanoTime()))return;
        var task=player.getScheduler().run(this,ignored->{
            long delay=ObserverAdmissions.UNSUPPORTED_NANOS;
            try{
                if(!running||!player.isOnline())return;
                var channels=capabilities(player);
                if(!channels.contains(STATE_CHANNEL)&&!channels.contains(EmoteWire.STATE_CHANNEL)&&!channels.contains(EmoteWire.EXTENDED_STATE_CHANNEL))return;
                delay=ObserverAdmissions.RETRY_NANOS;
                var peer=attach(player);if(peer==null)return;peer.channels=channels;
                delay=0;
                route(ledger.observe(peer.handle,position(peer),System.nanoTime()));refreshViewers(peer);
                if(source!=null)refreshOwned(source);
            }finally{observers.finish(id,System.nanoTime(),delay);}
        },()->observers.release(id));
        if(task==null)observers.release(id);
    }
    private void refreshOwned(Peer peer){
        if(!current(peer)||!peer.refreshing.compareAndSet(false,true))return;
        owned(peer,()->{try{refreshViewers(peer);}finally{peer.refreshing.set(false);}});
    }
    static boolean available(Player player){return player.isOnline()&&!player.isDead()&&player.getGameMode()!=GameMode.SPECTATOR&&!player.isSleeping()&&!player.isInsideVehicle()&&!player.isGliding();}
    private SocialCoordinator.Position position(Peer peer){
        var location=peer.player.getLocation();peer.world=peer.player.getWorld().getUID();
        return new SocialCoordinator.Position(peer.world,location.getX(),location.getY(),location.getZ(),available(peer.player),System.nanoTime());
    }
    private void watch(Peer peer){
        if(!current(peer))return;
        var snapshot=ledger.snapshot(peer.handle);if(snapshot==null)return;
        peer.ticks+=2;
        boolean worldV2=peer.channels.contains(ru.he3hauka.toolvisuals.network.world.WorldContextWire.EXTENDED_CHANNEL);
        boolean worldV3=peer.channels.contains(ru.he3hauka.toolvisuals.network.world.WorldContextWire.NAMED_CHANNEL);
        if(worldV3||worldV2||peer.channels.contains(ru.he3hauka.toolvisuals.network.world.WorldContextWire.CHANNEL))peer.worldContext.send(this,peer.player,peer.ticks%20==0,worldV3?3:worldV2?2:1);
        if(snapshot.state().mode()==Mode.PAIR&&!visible(peer,snapshot.state().pair().partner())){
            route(ledger.cancelExpected(peer.handle,snapshot.state(),false,System.nanoTime()));return;
        }
        if(snapshot.state().mode()==Mode.STOPPED&&peer.ticks%20!=0)return;
        capabilities(peer);route(ledger.observe(peer.handle,position(peer),System.nanoTime()));
        if(peer.ticks%20==0){
            boolean using = supportsState(peer) && (ledger.registered(peer.handle) || !peer.channels.contains(STATE_CHANNEL));
            clients.update(peer.id, peer.handle.epoch(), using);
            if (rewards != null) rewards.observe(peer.id, peer.handle.epoch(), using);
            if(peer.presence.requested.get()&&System.nanoTime()-peer.presence.lastRequest>90_000_000_000L){peer.presence.requested.set(false);peer.presence.outgoing.subscribe(false);}
            refreshViewers(peer);refreshPresence(peer,false);
        }
    }
    private void refreshViewers(Peer peer){
        if(!current(peer))return;
        var viewers=new HashSet<UUID>();
        for(var observer:peer.player.getTrackedBy()){
            var recipient=peers.get(observer.getUniqueId());
            if(recipient==null)discover(observer,peer);
            if(recipient!=null&&supportsState(recipient))viewers.add(recipient.id);
        }
        var previous=peer.viewers;
        peer.viewers=Set.copyOf(viewers);
        for(var old:previous)if(!viewers.contains(old)){var recipient=peers.get(old);if(recipient!=null){recipient.outgoing.forget(peer.id);recipient.cape.outgoing.forget(peer.id);recipient.accessory.outgoing.forget(peer.id);}}
        for(var id:viewers){var recipient=peers.get(id);if(recipient!=null)snapshot(peer,recipient);}
        snapshot(peer,peer);
    }
    private boolean supportsState(Peer peer){var c=peer.channels;return c.contains(STATE_CHANNEL)||c.contains(EmoteWire.STATE_CHANNEL)||c.contains(EmoteWire.EXTENDED_STATE_CHANNEL);}
    private void snapshot(Peer actor,Peer recipient){
        var state=ledger.snapshot(actor.handle);
        if(state!=null&&state.state().mode()!=Mode.STOPPED)enqueue(recipient,state);
        if(state!=null&&state.state().mode()==Mode.PAIR&&!recipient.channels.contains(STATE_CHANNEL)){
            var pair=ledger.pair(actor.id);if(pair!=null)notice(recipient,actor.id,pair.state(System.nanoTime()));
        }
        enqueueCape(recipient,capes.state(actor.id));
        enqueueAccessory(recipient,accessories.state(actor.id));
        var badge=ledger.badge(actor.id);if(badge!=null&&recipient.channels.contains(SocialProtocol.STATE_CHANNEL)){recipient.notice(actor.id,badge);schedule(recipient,false);}
    }
    @Override public void onPluginMessageReceived(String channel,Player player,byte[] bytes){
        if(!running)return;
        var input=RequestAdmission.Channel.byId(channel);
        if(input==null)return;
        var admission=ingress.admit(player.getUniqueId(),input,bytes==null?0:bytes.length,System.nanoTime());
        if(admission==RequestAdmission.Decision.THROTTLED){throttled.increment();return;}
        if(admission==RequestAdmission.Decision.MALFORMED){malformed.increment();return;}
        if(channel.equals(ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.REQUEST_CHANNEL)){minigames.receive(player,bytes);return;}
        if(channel.equals(SocialCommandWire.REQUEST_CHANNEL)){receiveSocialCommand(player,bytes);return;}
        if(channel.equals(AccessoryWire.REQUEST_CHANNEL)){receiveAccessory(player,bytes);return;}
        if(channel.equals(PresenceWire.REQUEST_CHANNEL)){receivePresence(player,bytes);return;}
        if(channel.equals(ru.he3hauka.toolvisuals.network.friends.FriendWire.REQUEST_CHANNEL)){friendRelay.receive(player,bytes);return;}
        if(channel.equals(CapeTransferWire.REQUEST_CHANNEL)){receiveCapeFragment(player,bytes);return;}
        if(channel.equals(CapeWire.REQUEST_CHANNEL)||channel.equals(CapeWire.ANIMATED_REQUEST_CHANNEL)){receiveCape(player,bytes,channel.equals(CapeWire.ANIMATED_REQUEST_CHANNEL));return;}
        boolean social=channel.equals(SocialProtocol.REQUEST_CHANNEL);
        if(!social&&!channel.equals(REQUEST_CHANNEL)&&!channel.equals(EmoteWire.REQUEST_CHANNEL)&&!channel.equals(EmoteWire.EXTENDED_REQUEST_CHANNEL))return;
        final Object decoded;
        try{decoded=social?SocialProtocol.decodeRequest(bytes):channel.equals(REQUEST_CHANNEL)?PlaybackWire.decodeRequest(bytes):EmoteWire.decodeRequest(bytes);}
        catch(RuntimeException error){malformed.increment();return;}
        var peer=attach(player);if(peer==null)return;
        long now=System.nanoTime();
        if(!peer.accept(social,now)){throttled.increment();return;}
        if(social){
            if(!limits.request(now)){throttled.increment();return;}
            var request=(SocialProtocol.Request)decoded;
            int key=request instanceof SocialProtocol.Preference?0:request instanceof SocialProtocol.Invite?1:request instanceof SocialProtocol.Answer?2:3;
            if(peer.social.offer(new UUID(0,key),request))owned(peer,()->{for(var value:peer.social.drain()){capabilities(peer);boolean visible=visiblePairTarget(peer,value);route(ledger.social(peer.handle,value,position(peer),!(value instanceof SocialProtocol.Answer answer)||!answer.accept()||visible&&limits.start(System.nanoTime()),visible,System.nanoTime()));}});
            return;
        }
        final Request request;
        if(decoded instanceof EmoteWire.Request legacy){
            synchronized(peer){long id=++peer.legacySequence;if(legacy.emote()<0){if(peer.legacyPlayback==0)return;request=new Stop(id,peer.handle.epoch(),peer.legacyPlayback);}else{peer.legacyPlayback=id;request=new Start(id,peer.handle.epoch(),legacy.emote(),legacy.cancelMoving());}}
        }else request=(Request)decoded;
        if(request instanceof Hello&&!limits.request(now)){throttled.increment();return;}
        boolean allowed=!(request instanceof Start)||limits.request(now)&&limits.start(now);
        if(peer.incoming.offer(request,allowed))owned(peer,()->{
            capabilities(peer);
            for(var command:peer.incoming.drain()){
                route(ledger.request(peer.handle,command.request(),position(peer),command.startAllowed(),System.nanoTime()));
                if(command.request() instanceof Hello)peer.outgoing.replay();
            }
            if(peer.pending())schedule(peer,false);
            refreshViewers(peer);
        });
    }
    private void receiveSocialCommand(Player player,byte[] bytes){
        final SocialCommandWire.Request request;
        try{request=SocialCommandWire.request(bytes);}catch(RuntimeException error){malformed.increment();return;}
        var peer=attach(player);if(peer==null)return;
        long now=System.nanoTime();
        if(!peer.accept(true,now)||!limits.request(now)){throttled.increment();return;}
        if(peer.socialCommands.offer(peer.id,request))owned(peer,()->{
            capabilities(peer);
            for(var next:peer.socialCommands.drain()){
                boolean visible=visiblePairTarget(peer,next.action());
                boolean allowed=!(next.action() instanceof SocialProtocol.Answer answer)||!answer.accept()||visible&&limits.start(System.nanoTime());
                route(ledger.socialCommand(peer.handle,next,position(peer),allowed,visible,System.nanoTime()));
            }
        });
    }
    private void receivePresence(Player player,byte[] bytes){
        final PresenceWire.Subscription request;
        try{request=PresenceWire.subscription(bytes);}catch(RuntimeException error){malformed.increment();return;}
        boolean enabled=request!=PresenceWire.Subscription.OFF;
        var peer=enabled?attach(player):peers.get(player.getUniqueId());
        if(peer==null||peer.player!=player||!current(peer))return;
        if(enabled){
            if(!presence.accept(peer.presence,System.nanoTime())){throttled.increment();return;}
            peer.presence.lastRequest=System.nanoTime();peer.presence.requested.set(true);
        }else if(!peer.presence.requested.getAndSet(false))return;
        if(peer.presence.incoming.offer(peer.id,request))owned(peer,()->{
            capabilities(peer);
            for(var value:peer.presence.incoming.drain()){
                peer.presence.outgoing.subscribe(value!=PresenceWire.Subscription.OFF&&peer.presence.requested.get()
                        &&peer.channels.contains(PresenceWire.STATE_CHANNEL)&&ledger.registered(peer.handle));
                if(value!=PresenceWire.Subscription.OFF){
                    refreshPresence(peer,true);
                    if(value==PresenceWire.Subscription.START)peer.presence.outgoing.replay(System.nanoTime());
                }
            }
            if(peer.presence.outgoing.next()!=null)schedulePresence(peer);
            else presence.idle(peer.id);
        });
    }
    private boolean visible(Peer viewer,UUID actor){
        if(viewer.id.equals(actor))return true;
        var target=peers.get(actor);
        return target!=null&&current(target)&&viewer.player.canSee(target.player);
    }
    private boolean visiblePairTarget(Peer peer,SocialProtocol.Request request){
        if(request instanceof SocialProtocol.Invite invite)return visible(peer,invite.target());
        if(request instanceof SocialProtocol.Answer answer&&answer.accept()){
            var sender=ledger.invitationSender(peer.handle,answer.invitation());
            return sender!=null&&visible(peer,sender);
        }
        return true;
    }
    private void refreshPresence(Peer peer,boolean force){
        if(!current(peer)||!peer.presence.outgoing.subscribed()||!peer.channels.contains(PresenceWire.STATE_CHANNEL))return;
        long now=System.nanoTime();var published=presence.directory.snapshot(now);
        if(!force&&peer.presence.observed==published&&now-peer.presence.nextVisibilityCheck<0)return;
        var visible=new ArrayList<UUID>();
        for(var entry:published.entrySet()){
            var actor=peers.get(entry.getKey());
            if(actor!=null&&current(actor)&&actor.handle.epoch()==entry.getValue()&&presence.directory.advertised(actor.id,actor.handle.epoch())
                    &&(actor==peer||peer.player.canSee(actor.player)))visible.add(actor.id);
        }
        peer.presence.observed=published;peer.presence.nextVisibilityCheck=now+5_000_000_000L;
        peer.presence.outgoing.offer(visible,now);
        if(peer.presence.outgoing.next()!=null)schedulePresence(peer);
    }
    private void schedulePresence(Peer peer){
        if(!current(peer)||!peer.presence.scheduled.compareAndSet(false,true))return;
        var task=peer.player.getScheduler().runDelayed(this,ignored->{
            peer.presence.scheduled.set(false);
            if(!current(peer))return;
            drainPresence(peer);
            if(peer.presence.outgoing.next()!=null)schedulePresence(peer);
        },()->retire(peer),5);
        if(task==null){peer.presence.scheduled.set(false);retire(peer);}
    }
    private void drainPresence(Peer peer){
        if(!peer.channels.contains(PresenceWire.STATE_CHANNEL)){peer.presence.outgoing.clear();presence.idle(peer.id);return;}
        for(int count=0;count<2;count++){
            var page=peer.presence.outgoing.next();if(page==null)break;
            if(!presence.send(peer.id,System.nanoTime())){deferred.increment();break;}
            boolean hidden=false;
            for(var id:page.players()){var actor=peers.get(id);if(actor!=null&&actor!=peer&&!peer.player.canSee(actor.player)){hidden=true;break;}}
            if(hidden){peer.presence.outgoing.invalidate();refreshPresence(peer,true);break;}
            peer.player.sendPluginMessage(this,PresenceWire.STATE_CHANNEL,PresenceWire.encode(page));
            sent.increment();peer.presence.outgoing.sent(page);
        }
        if(peer.presence.outgoing.next()==null)presence.idle(peer.id);
    }
    private void receiveAccessory(Player player,byte[] bytes){
        try{AccessoryWire.checkRequest(bytes);}catch(RuntimeException error){malformed.increment();return;}
        var peer=attach(player);if(peer==null)return;
        if(!accessories.accept(peer.accessory,System.nanoTime())){throttled.increment();return;}
        final AccessoryWire.Request request;
        try{request=AccessoryWire.request(bytes);}catch(RuntimeException error){malformed.increment();return;}
        if(peer.accessory.incoming.offer(peer.id,request))owned(peer,()->{
            capabilities(peer);
            for(var next:peer.accessory.incoming.drain()){
                var update=accessories.update(peer.id,peer.handle.epoch(),next);peer.accessory.acknowledge(update.ack());
                if(update.resync())peer.accessory.outgoing.replay();broadcastAccessory(peer,update.changed());
            }
            schedule(peer,false);
        });
    }
    private void broadcastAccessory(Peer actor,AccessoryWire.State state){
        if(state==null)return;
        for(var id:actor.viewers){var recipient=peers.get(id);if(recipient!=null)enqueueAccessory(recipient,state);}
    }
    private void enqueueAccessory(Peer peer,AccessoryWire.State state){
        if(state==null||state.actor().equals(peer.id)||!current(peer)||accessoryChannel(peer)==null||!accessories.receives(peer.id,peer.handle.epoch()))return;
        if(state.design()==null&&!peer.accessory.outgoing.contains(state.actor()))return;
        if(!peer.accessory.outgoing.offer(state,System.nanoTime())){deferred.increment();return;}schedule(peer,false);
    }
    private static String accessoryChannel(Peer peer){
        if(peer.channels.contains(AccessoryWire.EXPANDED_STATE_CHANNEL))return AccessoryWire.EXPANDED_STATE_CHANNEL;
        return peer.channels.contains(AccessoryWire.STATE_CHANNEL)?AccessoryWire.STATE_CHANNEL:null;
    }
    private void drainAccessories(Peer peer){
        for(int count=0;count<4;count++){
            var state=peer.accessory.outgoing.next();if(state==null)break;
            var channel=accessoryChannel(peer);
            if(channel==null||!accessories.receives(peer.id,peer.handle.epoch())){peer.accessory.outgoing.sent(state,System.nanoTime());continue;}
            if(state.design()!=null){
                var source=peers.get(state.actor());var latest=accessories.state(state.actor());
                if(source==null||latest==null||!current(source)||source.world==null||!source.world.equals(peer.world)||!source.viewers.contains(peer.id)||!peer.player.canSee(source.player)){peer.accessory.outgoing.discard(state);continue;}
                if(latest.epoch()!=state.epoch()||latest.revision()!=state.revision()){peer.accessory.outgoing.offer(latest,System.nanoTime());continue;}
            }
            if(!accessories.send(peer.id,System.nanoTime())){deferred.increment();break;}
            var delivered=channel.equals(AccessoryWire.STATE_CHANNEL)?AccessoryWire.legacy(state):state;
            peer.player.sendPluginMessage(this,channel,AccessoryWire.encode(delivered));sent.increment();peer.accessory.outgoing.sent(state,System.nanoTime());
        }
        if(peer.accessory.outgoing.next()==null)accessories.idle(peer.id);
    }
    private void receiveCape(Player player,byte[] bytes,boolean animated){
        try{if(animated)CapeWire.checkAnimatedRequest(bytes);else CapeWire.checkRequest(bytes);}catch(RuntimeException error){malformed.increment();return;}
        var peer=attach(player);if(peer==null)return;
        if(!capes.accept(peer.cape,System.nanoTime())){throttled.increment();return;}
        final CapeWire.Request request;
        try{request=animated?CapeWire.animatedRequest(bytes):CapeWire.request(bytes);}catch(RuntimeException error){malformed.increment();return;}
        acceptCape(peer,request);
    }
    private void receiveCapeFragment(Player player,byte[] bytes){
        final CapeTransferWire.RequestPart part;
        try{part=CapeTransferWire.request(bytes);}catch(IllegalArgumentException|java.nio.BufferUnderflowException error){malformed.increment();return;}
        var peer=attach(player);if(peer==null)return;
        final CapeRelay.Fragment transfer;
        try{transfer=capes.fragment(peer.cape,peer.id,peer.handle.epoch(),part,System.nanoTime());}catch(IllegalArgumentException error){malformed.increment();return;}
        if(!transfer.accepted()){throttled.increment();return;}
        if(transfer.bytes()==null)return;
        final CapeWire.Request request;
        try{request=CapeTransferWire.assembled(part,transfer.bytes());}catch(IllegalArgumentException|java.nio.BufferUnderflowException error){malformed.increment();return;}
        acceptCape(peer,request);
    }
    private void acceptCape(Peer peer,CapeWire.Request request){
        if(peer.cape.incoming.offer(peer.id,request))owned(peer,()->{
            capabilities(peer);
            for(var next:peer.cape.incoming.drain()){
                var update=capes.update(peer.id,peer.handle.epoch(),next);
                peer.cape.acknowledge(update.ack());
                if(update.resync())peer.cape.outgoing.replay();
                broadcastCape(peer,update.changed());
            }
            schedule(peer,false);
        });
    }
    private void broadcastCape(Peer actor,CapeWire.State state){
        if(state==null)return;
        for(var id:actor.viewers){var recipient=peers.get(id);if(recipient!=null)enqueueCape(recipient,state);}
    }
    private void enqueueCape(Peer peer,CapeWire.State state){
        if(state==null||!current(peer)||!capeSupported(peer)||!capes.receives(peer.id,peer.handle.epoch()))return;
        if(state.design()==null&&!peer.cape.outgoing.contains(state.actor()))return;
        if(!peer.cape.outgoing.offer(state,System.nanoTime())){deferred.increment();return;}
        schedule(peer,false);
    }
    private void owned(Peer peer,Runnable action){
        if(!current(peer))return;
        var task=peer.player.getScheduler().run(this,ignored->{if(current(peer))action.run();},()->retire(peer));
        if(task==null)retire(peer);
    }
    private static boolean capeSupported(Peer peer){return peer.channels.contains(CapeTransferWire.STATE_CHANNEL);}
    private void route(Iterable<PlaybackLedger.Effect> effects){
        if(!running)return;
        for(var effect:effects){
            if(effect instanceof PlaybackLedger.Changed changed){
                var actor=peers.get(changed.value().state().actor());if(actor==null)continue;
                enqueue(actor,changed.value());
                for(var id:actor.viewers){var recipient=peers.get(id);if(recipient!=null)enqueue(recipient,changed.value());}
                if(changed.value().state().mode()!=Mode.STOPPED)refreshOwned(actor);
            }else if(effect instanceof PlaybackLedger.Answered answered){var peer=peers.get(answered.recipient());if(peer!=null&&peer.channels.contains(REPLY_CHANNEL)){peer.reply(answered.reply());schedule(peer,false);}}
            else if(effect instanceof PlaybackLedger.SocialAnswered answered){var peer=peers.get(answered.recipient());if(peer!=null&&peer.channels.contains(SocialCommandWire.REPLY_CHANNEL)){peer.socialReply(answered.reply());schedule(peer,false);}}
            else if(effect instanceof PlaybackLedger.Social social){
                var delivery=social.delivery();
                if(delivery.state() instanceof SocialProtocol.Badge badge){
                    var actor=peers.get(badge.player());
                    if(actor!=null){var latest=ledger.badge(actor.id);presence.directory.advertise(actor.id,actor.handle.epoch(),current(actor)&&latest!=null&&latest.visible());}
                }
                if(delivery.recipient()!=null){notice(peers.get(delivery.recipient()),delivery.source(),delivery.state());}
                else {var actor=peers.get(delivery.source());if(actor!=null){notice(actor,delivery.source(),delivery.state());for(var id:actor.viewers)notice(peers.get(id),delivery.source(),delivery.state());}}
            }
        }
    }
    private void notice(Peer peer,UUID source,SocialProtocol.State state){
        if(peer==null||!current(peer)||!peer.channels.contains(SocialProtocol.STATE_CHANNEL))return;
        if((state instanceof SocialProtocol.Started||state instanceof SocialProtocol.Stopped)&&peer.channels.contains(STATE_CHANNEL))return;
        peer.notice(source,state);schedule(peer,false);
    }
    private void enqueue(Peer peer,PlaybackLedger.Published value){
        if(!current(peer)||!supportsState(peer))return;
        if(!peer.outgoing.offer(value,System.nanoTime())){deferred.increment();return;}
        schedule(peer,false);
    }
    private void schedule(Peer peer,boolean retry){
        if(!current(peer)||!peer.scheduled.compareAndSet(false,true))return;
        var task=peer.player.getScheduler().runDelayed(this,ignored->drain(peer),()->retire(peer),1);
        if(task==null){peer.scheduled.set(false);retire(peer);}
    }
    private void drain(Peer peer){
        if(!current(peer)){peer.scheduled.set(false);return;}
        capabilities(peer);
        boolean triedCape=false;
        for(int count=0;count<64;count++){
            Object control=peer.control();var value=control==null?peer.outgoing.next():null;
            if(control==null&&(value==null||count%8==7&&peer.cape.outgoing.next()!=null)){
                var cape=peer.cape.outgoing.next();if(cape==null)break;
                var source=peers.get(cape.actor());var latest=capes.state(cape.actor());
                if(!capeSupported(peer)||!capes.receives(peer.id,peer.handle.epoch())){peer.cape.outgoing.sent(cape,System.nanoTime());continue;}
                if(cape.design()!=null){
                    if(source==null||latest==null||!source.viewers.contains(peer.id)||!peer.player.canSee(source.player)){peer.cape.outgoing.discard(cape);continue;}
                    if(latest.epoch()!=cape.epoch()||latest.revision()!=cape.revision()){peer.cape.outgoing.offer(latest,System.nanoTime());continue;}
                }
                triedCape=true;
                if(!capes.send(peer.id,CapeRelay.deliveryBytes(cape),System.nanoTime())){deferred.increment();break;}
                for(var part:CapeTransferWire.split(cape)){peer.player.sendPluginMessage(this,CapeTransferWire.STATE_CHANNEL,CapeTransferWire.encode(part));sent.increment();}
                peer.cape.outgoing.sent(cape,System.nanoTime());continue;
            }
            String channel;byte[] bytes;UUID badgeActor=null;boolean badgeVisible=false;
            if(control instanceof SocialCommandWire.Reply reply){channel=SocialCommandWire.REPLY_CHANNEL;bytes=SocialCommandWire.encode(reply);}
            else if(control instanceof AccessoryWire.Ack ack){channel=AccessoryWire.ACK_CHANNEL;bytes=AccessoryWire.encode(ack);}
            else if(control instanceof CapeWire.Ack ack){channel=CapeWire.ACK_CHANNEL;bytes=CapeWire.encode(ack);}
            else if(control instanceof Reply reply){
                channel=REPLY_CHANNEL;var current=ledger.snapshot(peer.handle);
                bytes=PlaybackWire.encode(current==null?reply:new Reply(reply.requestId(),reply.operation(),reply.result(),ledger.registered(peer.handle),current.at(System.nanoTime())));
            }
            else if(control instanceof SocialNotice notice){
                var state=notice.state();
                channel=SocialProtocol.STATE_CHANNEL;
                if(!(state instanceof SocialProtocol.Badge)&&!visible(peer,notice.source())){peer.sent(control);continue;}
                if(state instanceof SocialProtocol.Invitation invite&&!visible(peer,invite.sender())){peer.sent(control);continue;}
                if(state instanceof SocialProtocol.Badge badge){
                    var latest=ledger.badge(badge.player());var source=peers.get(badge.player());
                    boolean visible=latest!=null&&latest.visible()&&source!=null&&(peer.id.equals(source.id)||source.viewers.contains(peer.id)&&peer.player.canSee(source.player));
                    if(!peer.permitsBadge(badge.player(),visible)){peer.sent(control);continue;}
                    badgeActor=badge.player();badgeVisible=visible;
                    state=new SocialProtocol.Badge(badge.player(),visible);
                }else if(state instanceof SocialProtocol.Started started){
                    if(!visible(peer,started.first())||!visible(peer,started.second())){peer.sent(control);continue;}
                    var pair=ledger.pair(started.first());
                    if(pair==null||!pair.id().equals(started.id())){peer.sent(control);continue;}
                    state=pair.state(System.nanoTime());
                }
                bytes=SocialProtocol.encode(state);
            }
            else {
                var state=value.at(System.nanoTime());var source=peers.get(state.actor());var latest=ledger.snapshot(new PlaybackLedger.Handle(state.actor(),state.epoch()));
                if(peer.channels.contains(STATE_CHANNEL)&&state.mode()==Mode.PAIR&&!visible(peer,state.pair().partner())){peer.outgoing.sent(value,System.nanoTime());continue;}
                if(state.mode()!=Mode.STOPPED&&(source==null||latest==null||latest.state().revision()!=state.revision()||!source.world.equals(peer.world)||!state.actor().equals(peer.id)&&(!source.viewers.contains(peer.id)||!peer.player.canSee(source.player)))){peer.outgoing.sent(value,System.nanoTime());continue;}
                if(peer.channels.contains(STATE_CHANNEL)){channel=STATE_CHANNEL;bytes=PlaybackWire.encode(state);}
                else {channel=peer.channels.contains(EmoteWire.EXTENDED_STATE_CHANNEL)?EmoteWire.EXTENDED_STATE_CHANNEL:EmoteWire.STATE_CHANNEL;var legacy=new EmoteWire.State(state.actor(),state.mode()==Mode.SOLO?state.emote():-1,state.mode()==Mode.SOLO?state.ageMillis()/50:0);bytes=EmoteWire.encode(EmoteWire.forRecipient(legacy,channel.equals(EmoteWire.EXTENDED_STATE_CHANNEL)));}
            }
            if(!peer.channels.contains(channel)){if(control!=null)peer.sent(control);else peer.outgoing.sent(value,System.nanoTime());continue;}
            if(limits.delivery(peer.id,System.nanoTime())!=RelayLimits.Delivery.SEND){deferred.increment();break;}
            peer.player.sendPluginMessage(this,channel,bytes);sent.increment();
            if(badgeActor!=null)peer.sentBadge(badgeActor,badgeVisible);
            if(control!=null)peer.sent(control);else peer.outgoing.sent(value,System.nanoTime());
        }
        drainAccessories(peer);
        drainPresence(peer);
        if(!triedCape||peer.cape.outgoing.next()==null)capes.idle(peer.id);
        if(peer.control()==null&&peer.outgoing.next()==null)limits.idle(peer.id);
        peer.scheduled.set(false);
        if(current(peer)&&peer.pending())schedule(peer,true);else limits.idle(peer.id);
    }
    private void cancel(Player player,boolean damage){
        var peer=peers.get(player.getUniqueId());if(peer==null||!current(peer))return;
        var expected=ledger.snapshot(peer.handle);
        route(ledger.cancelExpected(peer.handle,expected==null?null:expected.state(),damage,System.nanoTime()));
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void damage(EntityDamageEvent event){if(event.getEntity() instanceof Player player)cancel(player,true);}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void swing(PlayerAnimationEvent event){cancel(event.getPlayer(),false);}
    @EventHandler public void death(PlayerDeathEvent event){cancel(event.getEntity(),false);}
    @EventHandler(priority=EventPriority.MONITOR) public void hidden(PlayerHideEntityEvent event){
        if(!(event.getEntity() instanceof Player target))return;
        var peer=peers.get(event.getPlayer().getUniqueId());
        if(peer!=null&&current(peer))route(ledger.cancelForVisibility(peer.handle,target.getUniqueId(),System.nanoTime()));
    }
    @EventHandler public void quit(PlayerQuitEvent event){ingress.remove(event.getPlayer().getUniqueId());observers.forget(event.getPlayer().getUniqueId());var peer=peers.get(event.getPlayer().getUniqueId());if(peer!=null&&peer.player==event.getPlayer())retire(peer);}
    private void life(Player player){var peer=peers.get(player.getUniqueId());if(peer!=null&&peer.player==player){retire(peer);attach(player,peer);}}
    @EventHandler public void respawn(PlayerRespawnEvent event){life(event.getPlayer());}
    @EventHandler public void world(PlayerChangedWorldEvent event){life(event.getPlayer());}
    @EventHandler public void channel(PlayerRegisterChannelEvent event){if(!OUTPUT_CHANNELS.contains(event.getChannel()))return;observers.forget(event.getPlayer().getUniqueId());var peer=peers.get(event.getPlayer().getUniqueId());if(peer!=null)owned(peer,()->{capabilities(peer);refreshViewers(peer);});else discover(event.getPlayer());}
    @EventHandler public void unchannel(PlayerUnregisterChannelEvent event){if(!OUTPUT_CHANNELS.contains(event.getChannel()))return;var peer=peers.get(event.getPlayer().getUniqueId());if(peer!=null)owned(peer,()->capabilities(peer));}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void tracking(PlayerTrackEntityEvent event){
        if(!(event.getEntity() instanceof Player actor))return;
        var source=peers.get(actor.getUniqueId());var recipient=peers.get(event.getPlayer().getUniqueId());
        if(recipient==null)discover(event.getPlayer(),source);
        if(source==null||recipient==null)return;
        refreshOwned(source);
    }
    @EventHandler public void untracking(PlayerUntrackEntityEvent event){var recipient=peers.get(event.getPlayer().getUniqueId());if(recipient!=null){recipient.outgoing.forget(event.getEntity().getUniqueId());recipient.cape.outgoing.forget(event.getEntity().getUniqueId());recipient.accessory.outgoing.forget(event.getEntity().getUniqueId());}}
}

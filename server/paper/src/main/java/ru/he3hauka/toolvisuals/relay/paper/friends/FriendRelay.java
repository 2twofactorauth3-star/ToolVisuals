package ru.he3hauka.toolvisuals.relay.paper.friends;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import ru.he3hauka.toolvisuals.animation.network.PacketBudget;
import ru.he3hauka.toolvisuals.animation.network.RelayLimits;
import ru.he3hauka.toolvisuals.network.friends.FriendSession;
import ru.he3hauka.toolvisuals.network.friends.FriendWire;
import static ru.he3hauka.toolvisuals.network.friends.FriendWire.*;

public final class FriendRelay implements Listener,AutoCloseable {
    private record Watch(Actor viewer,int fields){}
    private record Frame(UUID session,float health,float maximum,float absorption,List<Item> armor,List<Item> inventory){}
    private static final class Sent {
        UUID session;
        State status;
        List<Item> inventory;
    }
    private static final class Actor {
        final Player player;
        final UUID id;
        final FriendSession model=new FriendSession();
        final PacketBudget input=new PacketBudget(8,4,System.nanoTime());
        final AtomicBoolean captureScheduled=new AtomicBoolean(),deliveryScheduled=new AtomicBoolean();
        final LinkedHashMap<UUID,Frame> pending=new LinkedHashMap<>();
        final Map<UUID,Sent> sent=new HashMap<>();
        volatile Frame frame;
        volatile boolean published;
        long sequence,nextInventory;
        Actor(Player player){this.player=player;id=player.getUniqueId();}
        synchronized boolean accept(long now){return input.allow(now);}
        synchronized void offer(UUID id,Frame frame){if(pending.size()<MAX_FRIENDS||pending.containsKey(id))pending.put(id,frame);}
        synchronized Map<UUID,Frame> pending(){return new LinkedHashMap<>(pending);}
        synchronized boolean hasPending(){return !pending.isEmpty();}
        synchronized void consumed(UUID id,Frame frame){pending.remove(id,frame);}
        synchronized void reset(){pending.clear();sent.clear();published=false;frame=null;sequence=0;nextInventory=0;}
    }
    private final JavaPlugin plugin;
    private final UUID server;
    private final Map<UUID,Actor> actors=new ConcurrentHashMap<>();
    private final PacketBudget input=new PacketBudget(512,1024,System.nanoTime()),captures=new PacketBudget(256,256,System.nanoTime());
    private final RelayLimits output=new RelayLimits(System.nanoTime(),128,512,8,16);
    private final ScheduledTask ticker;
    private volatile boolean running=true;
    private int cursor;
    public FriendRelay(JavaPlugin plugin,UUID server){
        this.plugin=plugin;this.server=server;plugin.getServer().getPluginManager().registerEvents(this,plugin);
        ticker=plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin,task->tick(),20,20);
    }
    private synchronized boolean accept(Actor actor,long now){return input.available(now)&&actor.accept(now)&&input.allow(now);}
    public void receive(Player player,byte[] bytes){
        UUID id=player.getUniqueId();var actor=actors.get(id);
        if(actor==null)synchronized(actors){actor=actors.get(id);if(actor==null&&actors.size()<RelayLimits.MAX_PLAYERS){actor=new Actor(player);actors.put(id,actor);}}
        if(!running||actor==null||actor.player!=player||!accept(actor,System.nanoTime()))return;
        final Request request;
        try{request=FriendWire.request(bytes);}catch(IllegalArgumentException|java.nio.BufferUnderflowException invalid){return;}
        var expected=actor;
        owner(actor,()->{
            UUID before=expected.model.session();boolean accepted=expected.model.apply(request,System.nanoTime());
            if(accepted&&request.operation()==Operation.HELLO&&!before.equals(request.session()))expected.reset();
            if(accepted&&request.operation()==Operation.BYE)expected.reset();
            if(expected.player.getListeningPluginChannels().contains(STATE_CHANNEL))expected.player.sendPluginMessage(plugin,STATE_CHANNEL,FriendWire.encode(FriendWire.reply(server,request,accepted)));
        });
    }
    private boolean current(Actor actor){return running&&actors.get(actor.id)==actor;}
    private void owner(Actor actor,Runnable action){
        if(!current(actor))return;
        var task=actor.player.getScheduler().run(plugin,ignored->{
            if(!current(actor)||!actor.player.isOnline())return;
            try{action.run();}catch(RuntimeException error){plugin.getLogger().log(java.util.logging.Level.WARNING,"Could not synchronize friend HUD",error);}
        },()->remove(actor.player));
        if(task==null)remove(actor.player);
    }
    private boolean mutual(Actor viewer,Actor source,long now){return current(viewer)&&source!=null&&current(source)&&FriendSession.mutual(viewer.model,viewer.id,source.model,source.id,now);}
    private void tick(){
        if(!running)return;long now=System.nanoTime();var watching=new HashMap<UUID,List<Watch>>();
        for(var viewer:actors.values()){
            for(var link:viewer.model.contacts(now).entrySet()){
                var source=actors.get(link.getKey());if(link.getValue()!=0&&mutual(viewer,source,now))watching.computeIfAbsent(source.id,key->new ArrayList<>()).add(new Watch(viewer,link.getValue()));
            }
            if(viewer.published||viewer.hasPending())schedule(viewer,false);
        }
        var ids=watching.keySet().stream().sorted().toList();if(ids.isEmpty()){cursor=0;return;}
        int attempted=0;
        while(attempted<ids.size()&&captures.allow(now)){
            UUID id=ids.get((cursor+attempted++)%ids.size());var source=actors.get(id);var viewers=List.copyOf(watching.get(id));
            if(source==null||!source.captureScheduled.compareAndSet(false,true))continue;
            owner(source,()->{
                try{capture(source,viewers);}finally{source.captureScheduled.set(false);}
            });
        }
        cursor=(cursor+attempted)%ids.size();
    }
    private void capture(Actor source,List<Watch> viewers){
        long now=System.nanoTime();int wanted=0;for(var watch:viewers)if(mutual(watch.viewer,source,now))wanted|=watch.fields;
        if(wanted==0)return;
        var inventory=source.player.getInventory();var previous=source.frame;
        List<Item> armor=(wanted&ARMOR)!=0?List.of(item(inventory.getHelmet()),item(inventory.getChestplate()),item(inventory.getLeggings()),item(inventory.getBoots())):List.of();
        List<Item> items=previous==null?List.of():previous.inventory;
        if((wanted&INVENTORY)!=0&&(items.isEmpty()||now-source.nextInventory>=0)){
            var next=new ArrayList<Item>(37);for(int slot=0;slot<36;slot++)next.add(item(inventory.getItem(slot)));next.add(item(inventory.getItemInOffHand()));items=List.copyOf(next);source.nextInventory=now+2_000_000_000L;
        }
        float maximum=number(source.player.getMaxHealth()),health=Math.min(maximum,number(source.player.getHealth())),absorption=number(source.player.getAbsorptionAmount());
        var frame=new Frame(source.model.session(),health,maximum,absorption,armor,items);if(frame.equals(previous))frame=previous;source.frame=frame;
        for(var watch:viewers)if(current(watch.viewer)){watch.viewer.offer(source.id,frame);schedule(watch.viewer,false);}
    }
    private static float number(double value){return Double.isFinite(value)?(float)Math.clamp(value,0,2048):0;}
    private static Item item(ItemStack stack){
        if(stack==null||stack.getType().isAir()||stack.getAmount()<=0)return Item.EMPTY;
        int maximum=Math.max(0,stack.getType().getMaxDurability()),damage=maximum>0?Math.clamp(Short.toUnsignedInt(stack.getDurability()),0,maximum):0;
        return new Item(stack.getType().getKey().toString(),Math.min(127,stack.getAmount()),damage,maximum,!stack.getEnchantments().isEmpty());
    }
    private void schedule(Actor viewer,boolean retry){
        if(!current(viewer)||!viewer.deliveryScheduled.compareAndSet(false,true))return;
        var task=viewer.player.getScheduler().runDelayed(plugin,ignored->{
            boolean remaining=false;
            try{if(current(viewer)&&viewer.player.isOnline())remaining=flush(viewer);}
            catch(RuntimeException error){plugin.getLogger().log(java.util.logging.Level.WARNING,"Could not deliver friend HUD",error);}
            finally{viewer.deliveryScheduled.set(false);}
            if(remaining||viewer.hasPending())schedule(viewer,true);else output.idle(viewer.id);
        },()->remove(viewer.player),retry?2:1);
        if(task==null){viewer.deliveryScheduled.set(false);remove(viewer.player);}
    }
    private boolean send(Actor viewer,State state){
        if(output.delivery(viewer.id,System.nanoTime())!=RelayLimits.Delivery.SEND)return false;
        viewer.player.sendPluginMessage(plugin,STATE_CHANNEL,FriendWire.encode(state));return true;
    }
    private boolean visible(Actor viewer,Actor source){return mutual(viewer,source,System.nanoTime())&&viewer.player.canSee(source.player);}
    private boolean flush(Actor viewer){
        if(!viewer.player.getListeningPluginChannels().contains(STATE_CHANNEL)){viewer.sent.clear();viewer.published=false;return false;}
        for(UUID id:List.copyOf(viewer.sent.keySet())){
            if(visible(viewer,actors.get(id)))continue;
            var previous=viewer.sent.get(id);if(previous.status==null&&previous.inventory==null){viewer.sent.remove(id);continue;}
            var clear=new State(server,viewer.model.session(),id,++viewer.sequence,Kind.CLEAR,0,0,0,0,List.of());
            if(!send(viewer,clear))return true;viewer.sent.remove(id);
        }
        int count=0;
        for(var entry:viewer.pending().entrySet()){
            if(count++>=16)return true;UUID id=entry.getKey();var frame=entry.getValue();var source=actors.get(id);
            if(!visible(viewer,source)||source.frame!=frame){viewer.consumed(id,frame);continue;}
            int fields=viewer.model.fields(id,System.nanoTime());var prior=viewer.sent.computeIfAbsent(id,key->new Sent());
            boolean changedSession=!frame.session.equals(prior.session);if(changedSession){prior.session=frame.session;prior.status=null;prior.inventory=null;}int statusFields=fields&3;if(frame.armor.size()!=4)statusFields&=~ARMOR;
            if(statusFields!=0){
                var status=new State(server,viewer.model.session(),id,1,Kind.STATUS,statusFields,(statusFields&HEALTH)!=0?frame.health:0,(statusFields&HEALTH)!=0?frame.maximum:0,(statusFields&HEALTH)!=0?frame.absorption:0,(statusFields&ARMOR)!=0?frame.armor:List.of());
                if(changedSession||!status.equals(prior.status)){
                    if(!send(viewer,new State(server,status.session(),id,++viewer.sequence,status.kind(),status.fields(),status.health(),status.maximum(),status.absorption(),status.items())))return true;prior.status=status;viewer.published=true;
                }
            }
            if((fields&INVENTORY)!=0&&frame.inventory.size()==37){
                if(changedSession||!frame.inventory.equals(prior.inventory)){
                    if(!send(viewer,new State(server,viewer.model.session(),id,++viewer.sequence,Kind.INVENTORY,INVENTORY,0,0,0,frame.inventory)))return true;prior.inventory=frame.inventory;viewer.published=true;
                }
            }else prior.inventory=null;
            prior.session=frame.session;viewer.consumed(id,frame);
        }
        viewer.published=viewer.sent.values().stream().anyMatch(value->value.status!=null||value.inventory!=null);return false;
    }
    @EventHandler public void quit(PlayerQuitEvent event){remove(event.getPlayer());}
    private void remove(Player player){
        var actor=actors.get(player.getUniqueId());if(actor==null||actor.player!=player||!actors.remove(actor.id,actor))return;
        actor.model.close();output.remove(actor.id);
    }
    @Override public void close(){running=false;ticker.cancel();for(var actor:actors.values())actor.model.close();actors.clear();}
}

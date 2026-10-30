package ru.he3hauka.toolvisuals.animation.protocol.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ru.he3hauka.toolvisuals.animation.Emote;
import ru.he3hauka.toolvisuals.animation.network.RelayLimits;
import ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire;
import ru.he3hauka.toolvisuals.animation.protocol.social.SocialCommandWire;
import ru.he3hauka.toolvisuals.animation.social.SocialCoordinator;
import ru.he3hauka.toolvisuals.animation.social.SocialProtocol;

import static ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire.*;

public final class PlaybackLedger {
    public record Handle(UUID actor,long epoch){}
    public record Published(State state,long began) {
        public State at(long now){return state.mode()==Mode.STOPPED?state:state.aged((int)Math.clamp((now-began)/1_000_000L,0,state.durationMillis()-1));}
    }
    public sealed interface Effect permits Changed,Answered,Social,SocialAnswered {}
    public record Changed(Published value) implements Effect {}
    public record Answered(UUID recipient,Reply reply) implements Effect {}
    public record Social(SocialCoordinator.Delivery delivery) implements Effect {}
    public record SocialAnswered(UUID recipient,SocialCommandWire.Reply reply) implements Effect {}
    public record Opened(Handle handle,List<Effect> effects){}
    private static final class Actor {
        final Handle handle;
        Published current;
        SocialCoordinator.Position position,origin;
        long command,hello,lastStart=Long.MIN_VALUE,lastDamage=Long.MIN_VALUE;
        boolean cancelMoving;
        Request lastRequest;
        Result lastResult;
        long socialCommand,socialActivation;
        SocialCommandWire.Request lastSocialRequest;
        SocialCommandWire.Result lastSocialResult;
        Actor(UUID server,Handle handle){this.handle=handle;current=new Published(new State(server,handle.actor,handle.epoch,0,Mode.STOPPED,0,-1,0,null),0);}
    }
    public final UUID server=UUID.randomUUID();
    private final Map<UUID,Actor> actors=new HashMap<>();
    private final SocialCoordinator social=new SocialCoordinator();
    private long epoch;
    public synchronized Opened open(UUID id){
        var effects=new ArrayList<Effect>();var old=actors.get(id);
        if(old!=null)close(old,effects);
        if(actors.size()>=RelayLimits.MAX_PLAYERS)return null;
        var handle=new Handle(id,++epoch);actors.put(id,new Actor(server,handle));return new Opened(handle,List.copyOf(effects));
    }
    private Actor actor(Handle handle){var actor=actors.get(handle.actor);return actor!=null&&actor.handle.equals(handle)?actor:null;}
    public synchronized Published snapshot(Handle handle){var actor=actor(handle);return actor==null?null:actor.current;}
    public synchronized Published snapshot(UUID id){var actor=actors.get(id);return actor==null?null:actor.current;}
    public synchronized int size(){return actors.size();}
    public synchronized int active(){return (int)actors.values().stream().filter(actor->actor.current.state.mode()!=Mode.STOPPED).count();}
    public synchronized boolean registered(Handle handle){return actor(handle)!=null&&social.registered(handle.actor);}

    public synchronized List<Effect> request(Handle handle,Request request,SocialCoordinator.Position position,boolean startAllowed,long now){
        var actor=actor(handle);if(actor==null)return List.of();var effects=new ArrayList<Effect>();actor.position=effective(actor,position,now);position=actor.position;
        if(request instanceof Hello hello){
            if(hello.requestId()>actor.hello){actor.hello=hello.requestId();collectSocial(social.register(handle.actor,position,hello.badge()),now,effects);}
            answer(actor,request,Result.ACCEPTED,effects,now);return effects;
        }
        if(request.epoch()!=handle.epoch){answer(actor,request,Result.STALE,effects,now);return effects;}
        if(request.equals(actor.lastRequest)){answer(actor,request,actor.lastResult,effects,now);return effects;}
        if(request.requestId()<=actor.command){answer(actor,request,Result.STALE,effects,now);return effects;}
        actor.command=request.requestId();actor.lastRequest=request;actor.lastResult=Result.ACCEPTED;
        if(request instanceof Start start){
            actor.socialActivation=0;
            cancel(actor,effects,now);
            Result result=!available(actor)?Result.UNAVAILABLE:actor.lastStart!=Long.MIN_VALUE&&now-actor.lastStart<500_000_000L?Result.COOLDOWN:!startAllowed?Result.GLOBAL_LIMIT:Result.ACCEPTED;
            if(result==Result.ACCEPTED){
                actor.lastStart=now;actor.origin=position;actor.cancelMoving=start.cancelMoving();
                publish(actor,new State(server,handle.actor,handle.epoch,nextRevision(actor),Mode.SOLO,start.requestId(),start.emote(),0,null),now,effects);
            }
            actor.lastResult=result;answer(actor,request,result,effects,now);
        }else if(request instanceof Stop stop){
            if(actor.current.state.mode()==Mode.SOLO&&actor.current.state.playbackId()==stop.playbackId())terminate(actor,effects);
            answer(actor,request,Result.ACCEPTED,effects,now);
        }else if(request instanceof StopPair stop){
            if(actor.current.state.mode()==Mode.PAIR&&actor.current.state.pair().id().equals(stop.pairId()))cancel(actor,effects,now);
            answer(actor,request,Result.ACCEPTED,effects,now);
        }
        return effects;
    }
    private void answer(Actor actor,Request request,Result result,List<Effect> effects,long now){effects.add(new Answered(actor.handle.actor,new Reply(request.requestId(),PlaybackWire.operation(request),result,social.registered(actor.handle.actor),actor.current.at(now))));}
    private static SocialCoordinator.Position effective(Actor actor,SocialCoordinator.Position position,long now){
        return new SocialCoordinator.Position(position.world(),position.x(),position.y(),position.z(),position.available()&&(actor.lastDamage==Long.MIN_VALUE||now-actor.lastDamage>=500_000_000L),position.observed());
    }
    private static boolean available(Actor actor){return actor.position!=null&&actor.position.available();}
    private static long nextRevision(Actor actor){return actor.current.state.revision()+1;}
    private void publish(Actor actor,State state,long began,List<Effect> effects){actor.current=new Published(state,began);effects.add(new Changed(actor.current));}
    private void terminate(Actor actor,List<Effect> effects){
        var old=actor.current.state;
        if(old.mode()==Mode.STOPPED)return;
        publish(actor,new State(server,actor.handle.actor,actor.handle.epoch,nextRevision(actor),Mode.STOPPED,old.playbackId(),-1,0,null),0,effects);
    }
    private void cancel(Actor actor,List<Effect> effects,long now){
        collectSocial(social.cancel(actor.handle.actor,SocialProtocol.Outcome.CANCELLED),now,effects);
        if(actor.current.state.mode()==Mode.SOLO)terminate(actor,effects);
    }
    public synchronized List<Effect> social(Handle handle,SocialProtocol.Request request,SocialCoordinator.Position position,long now){return social(handle,request,position,true,now);}
    public synchronized List<Effect> social(Handle handle,SocialProtocol.Request request,SocialCoordinator.Position position,boolean startAllowed,long now){
        return social(handle,request,position,startAllowed,true,now);
    }
    public synchronized List<Effect> social(Handle handle,SocialProtocol.Request request,SocialCoordinator.Position position,boolean startAllowed,boolean visible,long now){
        var actor=actor(handle);if(actor==null)return List.of();var effects=new ArrayList<Effect>();actor.position=effective(actor,position,now);position=actor.position;
        if(request instanceof SocialProtocol.Preference preference)collectSocial(social.register(handle.actor,position,preference.badge()),now,effects);
        else if(social.registered(handle.actor)){
            if(request instanceof SocialProtocol.Invite||request instanceof SocialProtocol.Answer)actor.socialActivation=0;
            collectSocial(social.update(handle.actor,position),now,effects);
            if(request instanceof SocialProtocol.Invite invite)collectSocial(social.invite(handle.actor,invite.target(),invite.emote(),visible,now),now,effects);
            else if(request instanceof SocialProtocol.Answer answer)collectSocial(social.answer(handle.actor,answer.invitation(),answer.accept(),startAllowed,visible,now),now,effects);
            else if(request instanceof SocialProtocol.Cancel)cancel(actor,effects,now);
        }
        return effects;
    }
    public synchronized List<Effect> socialCommand(Handle handle,SocialCommandWire.Request request,SocialCoordinator.Position position,boolean startAllowed,long now){
        return socialCommand(handle,request,position,startAllowed,true,now);
    }
    public synchronized List<Effect> socialCommand(Handle handle,SocialCommandWire.Request request,SocialCoordinator.Position position,boolean startAllowed,boolean visible,long now){
        var actor=actor(handle);if(actor==null)return List.of();
        var effects=new ArrayList<Effect>();SocialCommandWire.Result result;
        if(request.epoch()!=handle.epoch)result=SocialCommandWire.Result.STALE;
        else if(request.equals(actor.lastSocialRequest))result=actor.lastSocialResult;
        else if(request.id()<=actor.socialCommand)result=SocialCommandWire.Result.STALE;
        else if(!social.registered(handle.actor))result=SocialCommandWire.Result.UNREGISTERED;
        else {
            actor.socialCommand=request.id();actor.lastSocialRequest=request;result=SocialCommandWire.Result.ACCEPTED;
            if(request.action() instanceof SocialProtocol.Cancel){
                if(actor.socialActivation>0&&actor.socialActivation<=request.through()){
                    collectSocial(social.cancel(handle.actor,SocialProtocol.Outcome.CANCELLED),now,effects);actor.socialActivation=0;
                }
            }else {
                effects.addAll(social(handle,request.action(),position,startAllowed,visible,now));actor.socialActivation=request.id();
                if(request.action() instanceof SocialProtocol.Answer&&effects.stream().noneMatch(effect->effect instanceof Social value&&(value.delivery().state() instanceof SocialProtocol.Result||value.delivery().state() instanceof SocialProtocol.Started)))result=SocialCommandWire.Result.UNAVAILABLE;
            }
            actor.lastSocialResult=result;
        }
        effects.add(new SocialAnswered(handle.actor,new SocialCommandWire.Reply(server,request.id(),handle.epoch,result)));
        return List.copyOf(effects);
    }
    private void collectSocial(List<SocialCoordinator.Delivery> deliveries,long now,List<Effect> effects){
        var starts=new java.util.HashSet<UUID>();var stops=new java.util.HashSet<UUID>();
        for(var delivery:deliveries){
            if(delivery.state() instanceof SocialProtocol.Started started){if(starts.add(started.id()))pair(started,now,effects);effects.add(new Social(delivery));}
            else if(delivery.state() instanceof SocialProtocol.Stopped stopped){if(stops.add(stopped.id())){var actor=actors.get(delivery.source());if(actor!=null&&actor.current.state.mode()==Mode.PAIR&&actor.current.state.pair().id().equals(stopped.id())){var other=actors.get(actor.current.state.pair().partner());terminate(actor,effects);if(other!=null&&other.current.state.mode()==Mode.PAIR&&other.current.state.pair().id().equals(stopped.id()))terminate(other,effects);}}effects.add(new Social(delivery));}
            else effects.add(new Social(delivery));
        }
    }
    private void pair(SocialProtocol.Started started,long now,List<Effect> effects){
        var first=actors.get(started.first());var second=actors.get(started.second());if(first==null||second==null)return;
        long firstRevision=nextRevision(first),secondRevision=nextRevision(second);
        var firstPair=new Pair(started.id(),started.second(),second.handle.epoch,secondRevision,false);
        var secondPair=new Pair(started.id(),started.first(),first.handle.epoch,firstRevision,true);
        publish(first,new State(server,first.handle.actor,first.handle.epoch,firstRevision,Mode.PAIR,0,started.emote().id,started.age()*50,firstPair),now-started.age()*50_000_000L,effects);
        publish(second,new State(server,second.handle.actor,second.handle.epoch,secondRevision,Mode.PAIR,0,started.emote().id,started.age()*50,secondPair),now-started.age()*50_000_000L,effects);
    }
    public synchronized List<Effect> observe(Handle handle,SocialCoordinator.Position position,long now){
        var actor=actor(handle);if(actor==null)return List.of();var effects=new ArrayList<Effect>();actor.position=effective(actor,position,now);position=actor.position;
        collectSocial(social.update(handle.actor,position),now,effects);
        var state=actor.current.state;
        if(state.mode()==Mode.SOLO&&(!available(actor)||!position.world().equals(actor.origin.world())||now-actor.current.began>=state.durationMillis()*1_000_000L||actor.cancelMoving&&position.distanceSquared(actor.origin)>.012))terminate(actor,effects);
        return effects;
    }
    public synchronized List<Effect> expireInvitations(long now){var effects=new ArrayList<Effect>();collectSocial(social.tick(now),now,effects);return effects;}
    public synchronized List<Effect> cancelExpected(Handle handle,State expected,boolean damage,long now){
        var actor=actor(handle);if(actor==null)return List.of();var effects=new ArrayList<Effect>();
        if(damage){actor.lastDamage=now;if(actor.position!=null){actor.position=effective(actor,actor.position,now);collectSocial(social.update(handle.actor,actor.position),now,effects);}}
        var current=actor.current.state;
        if(expected!=null&&current.epoch()==expected.epoch()&&current.revision()==expected.revision())cancel(actor,effects,now);
        return effects;
    }
    public synchronized List<Effect> close(Handle handle){var actor=actor(handle);if(actor==null)return List.of();var effects=new ArrayList<Effect>();close(actor,effects);return effects;}
    public synchronized List<Effect> cancelForVisibility(Handle handle,UUID other,long now){
        var actor=actor(handle);if(actor==null||!social.involves(handle.actor,other))return List.of();
        var effects=new ArrayList<Effect>();collectSocial(social.cancel(handle.actor,SocialProtocol.Outcome.CANCELLED),now,effects);actor.socialActivation=0;
        return List.copyOf(effects);
    }
    private void close(Actor actor,List<Effect> effects){cancel(actor,effects,System.nanoTime());collectSocial(social.remove(actor.handle.actor),System.nanoTime(),effects);actors.remove(actor.handle.actor);}
    public synchronized SocialProtocol.Badge badge(UUID actor){return social.badge(actor);}
    public synchronized UUID invitationSender(Handle handle,UUID nonce){return actor(handle)==null?null:social.invitationSender(handle.actor,nonce);}
    public synchronized SocialCoordinator.Pair pair(UUID actor){return social.pair(actor);}
}

package ru.he3hauka.toolvisuals.animation.protocol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire.*;

public final class PlaybackReplica {
    public enum Phase { STOPPED,LOCAL,PENDING,ACCEPTED,PAIRED }
    public enum StopReason {
        USER(true),SYNC_DISABLED(true),DISABLED(true),INTERRUPTED(true),COMPLETED(false);
        public final boolean revokeConsent;
        StopReason(boolean revokeConsent){this.revokeConsent=revokeConsent;}
    }
    public record Change(State state,boolean stopLocal,boolean confirmed,boolean reset,Result reason) {
        public static final Change NONE=new Change(null,false,false,false,null);
    }
    private record Consent(long generation,long until){}
    public final UUID owner;
    private UUID server,pairId;
    private long epoch,sequence,wantedPlayback,hello,generation;
    private boolean ready;
    private Phase phase=Phase.STOPPED;
    private Request pending;
    private final Map<UUID,State> versions=new LinkedHashMap<>();
    private final java.util.Set<UUID> unloaded=new java.util.HashSet<>();
    private final Map<UUID,Long> cancelledPairs=new LinkedHashMap<>();
    private final Map<UUID,Consent> consents=new LinkedHashMap<>();

    public PlaybackReplica(UUID owner){this.owner=java.util.Objects.requireNonNull(owner);}
    private long next(){return ++sequence;}
    public boolean ready(){return ready;}
    public UUID server(){return server;}
    public long epoch(){return epoch;}
    public Phase phase(){return phase;}
    public long playback(){return wantedPlayback;}
    public UUID pair(){return pairId;}
    public void local(){generation++;consents.clear();if(pending instanceof Start)pending=null;wantedPlayback=0;pairId=null;phase=Phase.LOCAL;}
    public void revokeConsent(){generation++;consents.clear();}
    public Hello hello(boolean badge){hello=next();return new Hello(hello,badge);}
    public void beginLife(){ready=false;hello=0;versions.clear();unloaded.clear();consents.clear();cancelledPairs.clear();phase=Phase.STOPPED;pending=null;wantedPlayback=0;pairId=null;}
    public void consent(UUID partner,long now){consents.put(partner,new Consent(generation,now+10_000_000_000L));if(consents.size()>16)consents.remove(consents.keySet().iterator().next());}
    public Request start(int emote,boolean moving){
        generation++;consents.clear();pairId=null;wantedPlayback=next();phase=ready?Phase.PENDING:Phase.LOCAL;
        pending=ready?new Start(wantedPlayback,epoch,emote,moving):null;return pending;
    }
    public Request stop(long now){
        return stop(now,StopReason.USER);
    }
    public Request stop(long now,StopReason reason){
        if(reason.revokeConsent)revokeConsent();
        if((phase==Phase.STOPPED||phase==Phase.LOCAL)&&(pending instanceof Stop||pending instanceof StopPair))return pending;
        if(phase==Phase.PAIRED&&pairId!=null){
            cancelledPairs.put(pairId,now+15_000_000_000L);if(cancelledPairs.size()>256)cancelledPairs.remove(cancelledPairs.keySet().iterator().next());
            pending=ready?new StopPair(next(),epoch,pairId):null;
        }else if((phase==Phase.PENDING||phase==Phase.ACCEPTED)&&wantedPlayback>0)pending=ready?new Stop(next(),epoch,wantedPlayback):null;
        else pending=null;
        phase=Phase.STOPPED;return pending;
    }
    public Change reply(Reply reply,long now){
        if(!reply.state().actor().equals(owner))return Change.NONE;
        if(reply.operation()==Operation.HELLO){
            if(reply.requestId()!=hello||reply.result()!=Result.ACCEPTED||!reply.registered())return Change.NONE;
            boolean reset=server!=null&&(!server.equals(reply.state().server())||epoch!=reply.state().epoch());
            if(reset){beginLife();hello=reply.requestId();}
            server=reply.state().server();epoch=reply.state().epoch();ready=true;
            if(reset)return new Change(null,true,false,true,null);
            return Change.NONE;
        }
        if(!scope(reply.state())||pending==null||reply.requestId()!=pending.requestId()||reply.operation()!=PlaybackWire.operation(pending))return Change.NONE;
        var command=pending;pending=null;
        if(command instanceof Start start){
            if(phase!=Phase.PENDING&&phase!=Phase.ACCEPTED||wantedPlayback!=start.requestId())return Change.NONE;
            if(reply.result()==Result.ACCEPTED&&reply.state().mode()==Mode.SOLO&&reply.state().playbackId()==wantedPlayback){phase=Phase.ACCEPTED;remember(reply.state());return new Change(null,false,true,false,null);}
            if(reply.state().mode()==Mode.PAIR){var change=state(reply.state(),now);if(change!=Change.NONE)return change;}
            phase=Phase.STOPPED;remember(reply.state());return new Change(null,true,false,false,reply.result()==Result.ACCEPTED?null:reply.result());
        }
        return state(reply.state(),now);
    }
    public Change state(State value,long now){
        if(!scope(value))return Change.NONE;
        expire(now);
        var previous=versions.get(value.actor());
        if(older(value,previous))return Change.NONE;
        boolean repeated=same(value,previous)&&!unloaded.contains(value.actor());
        if(value.mode()==Mode.PAIR){
            var partner=value.partnerState();var known=versions.get(partner.actor());
            if(older(partner,known))return Change.NONE;
            if(repeated&&same(partner,known)&&!unloaded.contains(partner.actor()))return Change.NONE;
        }else if(repeated)return Change.NONE;
        State local=value.actor().equals(owner)?value:value.mode()==Mode.PAIR&&value.pair().partner().equals(owner)?value.partnerState():null;
        if(local!=null){
            if(local.epoch()!=epoch)return Change.NONE;
            if(local.mode()==Mode.PAIR){
                var consent=consents.get(local.pair().partner());
                if(cancelledPairs.containsKey(local.pair().id())||!(phase==Phase.PAIRED&&local.pair().id().equals(pairId))&&(consent==null||consent.generation!=generation))return Change.NONE;
                pairId=local.pair().id();phase=Phase.PAIRED;pending=null;
            }else if(local.mode()==Mode.SOLO){
                if((phase!=Phase.PENDING&&phase!=Phase.ACCEPTED)||local.playbackId()!=wantedPlayback)return Change.NONE;
                remember(value);return new Change(null,false,true,false,null);
            }else if((phase==Phase.PENDING||phase==Phase.ACCEPTED)&&local.playbackId()==wantedPlayback||phase==Phase.PAIRED&&local.playbackId()==0){phase=Phase.STOPPED;pending=null;pairId=null;}
            else return Change.NONE;
        }
        remember(value);if(value.mode()==Mode.PAIR)remember(value.partnerState());
        return new Change(value,false,false,false,null);
    }
    private boolean scope(State state){return ready&&server.equals(state.server());}
    private void remember(State state){
        var old=versions.get(state.actor());if(older(state,old))return;
        versions.put(state.actor(),state);
        unloaded.remove(state.actor());
        if(versions.size()>256){var iterator=versions.keySet().iterator();while(iterator.hasNext()){var id=iterator.next();if(!id.equals(owner)){iterator.remove();unloaded.remove(id);break;}}}
    }
    private static boolean older(State state,State old){return old!=null&&(state.epoch()<old.epoch()||state.epoch()==old.epoch()&&state.revision()<old.revision());}
    private static boolean same(State state,State old){return old!=null&&state.epoch()==old.epoch()&&state.revision()==old.revision();}
    public void unload(UUID actor){if(versions.containsKey(actor))unloaded.add(actor);}
    public void expire(long now){cancelledPairs.values().removeIf(until->until<=now);consents.values().removeIf(consent->consent.until<=now);}
}

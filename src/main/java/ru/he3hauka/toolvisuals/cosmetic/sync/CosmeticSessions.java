package ru.he3hauka.toolvisuals.cosmetic.sync;

import java.util.*;

public class CosmeticSessions<T,S extends CosmeticState<T>> {
    @FunctionalInterface public interface Factory<T,S>{S create(UUID server,UUID actor,long epoch,long revision,T design);}
    protected record Receipt<S>(long requestId,long epoch,long revision,boolean accepted,S changed,boolean resync){}
    private record Session<S>(long epoch,long requestId,boolean receive,S state){}
    protected final UUID server;
    private final int maximum;
    private final Factory<T,S> factory;
    private final Map<UUID,Session<S>> sessions=new HashMap<>();

    protected CosmeticSessions(UUID server,int maximum,Factory<T,S> factory){this.server=Objects.requireNonNull(server);if(maximum<1)throw new IllegalArgumentException();this.maximum=maximum;this.factory=Objects.requireNonNull(factory);}
    protected synchronized Receipt<S> update(UUID actor,long epoch,long requestId,long requestEpoch,boolean receive,T design){
        var old=sessions.get(actor);long revision=old==null||old.epoch!=epoch?0:old.state.revision();
        if(requestEpoch!=epoch||old!=null&&old.epoch>epoch||old==null&&sessions.size()>=maximum)return new Receipt<>(requestId,epoch,revision,false,null,false);
        if(old!=null&&old.epoch==epoch&&requestId<=old.requestId)return new Receipt<>(requestId,epoch,revision,requestId==old.requestId,null,false);
        boolean changed=old==null||old.epoch!=epoch||!Objects.equals(old.state.design(),design);
        S state=factory.create(server,actor,epoch,changed?revision+1:revision,design);
        boolean resync=old==null||old.epoch!=epoch||!old.receive&&receive;
        sessions.put(actor,new Session<>(epoch,requestId,receive,state));
        return new Receipt<>(requestId,epoch,state.revision(),true,changed?state:null,resync);
    }
    public synchronized S state(UUID actor){var session=sessions.get(actor);return session==null?null:session.state;}
    public synchronized boolean receives(UUID actor,long epoch){var session=sessions.get(actor);return session!=null&&session.epoch==epoch&&session.receive;}
    public synchronized S remove(UUID actor,long epoch){
        var old=sessions.get(actor);if(old==null||old.epoch!=epoch)return null;sessions.remove(actor);
        return factory.create(server,actor,epoch,old.state.revision()+1,null);
    }
    public synchronized int size(){return sessions.size();}
}

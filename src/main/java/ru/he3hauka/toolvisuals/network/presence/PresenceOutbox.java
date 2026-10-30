package ru.he3hauka.toolvisuals.network.presence;

import java.util.*;

public final class PresenceOutbox {
    private static final long REPLAY_NANOS=180_000_000_000L;
    private final UUID server;
    private final long epoch;
    private long revision,lastQueued;
    private boolean subscribed;
    private Set<UUID> current;
    private List<PresenceWire.Page> pending=List.of();
    private int cursor;

    public PresenceOutbox(UUID server,long epoch){this.server=Objects.requireNonNull(server);if(epoch<=0)throw new IllegalArgumentException();this.epoch=epoch;}
    public synchronized boolean subscribed(){return subscribed;}
    public synchronized void subscribe(boolean enabled){
        if(subscribed==enabled)return;
        subscribed=enabled;current=null;pending=List.of();cursor=0;
    }

    public synchronized void offer(Collection<UUID> players,long now){
        if(!subscribed)return;
        if(players.size()>PresenceWire.MAX_PLAYERS)throw new IllegalArgumentException("Too many presence entries");
        var next=Set.copyOf(players);
        if(cursor<pending.size()||next.equals(current)&&now-lastQueued<REPLAY_NANOS)return;
        var pages=PresenceWire.snapshot(server,epoch,revision+1,next);
        current=next;revision++;lastQueued=now;pending=pages;cursor=0;
    }

    public synchronized void replay(long now){
        if(!subscribed||current==null||cursor<pending.size()||now-lastQueued<5_000_000_000L)return;
        lastQueued=now;pending=PresenceWire.snapshot(server,epoch,++revision,current);cursor=0;
    }
    public synchronized void invalidate(){current=null;pending=List.of();cursor=0;}
    public synchronized PresenceWire.Page next(){return cursor<pending.size()?pending.get(cursor):null;}
    public synchronized void sent(PresenceWire.Page page){if(cursor<pending.size()&&pending.get(cursor)==page)cursor++;}
    public synchronized void clear(){subscribed=false;current=null;pending=List.of();cursor=0;}
}

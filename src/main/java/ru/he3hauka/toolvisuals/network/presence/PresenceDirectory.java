package ru.he3hauka.toolvisuals.network.presence;

import java.util.*;

public final class PresenceDirectory {
    private final Map<UUID,Long> entries=new HashMap<>();
    private Map<UUID,Long> snapshot=Map.of();
    private boolean dirty;
    private long refreshed;
    public synchronized void advertise(UUID player,long epoch,boolean visible){
        if(player==null||epoch<=0)throw new IllegalArgumentException();
        var previous=entries.get(player);
        if(previous!=null&&previous>epoch)return;
        boolean changed;
        if(visible){
            if(previous==null&&entries.size()>=PresenceWire.MAX_PLAYERS)return;
            changed=!Objects.equals(previous,epoch);entries.put(player,epoch);
        }else changed=entries.remove(player)!=null;
        dirty|=changed;
    }
    public synchronized Map<UUID,Long> snapshot(long now){
        if(dirty&&(refreshed==0||now-refreshed>=250_000_000L)){snapshot=Map.copyOf(entries);dirty=false;refreshed=now;}
        return snapshot;
    }
    public synchronized boolean advertised(UUID player,long epoch){return Objects.equals(entries.get(player),epoch);}
}

package ru.he3hauka.toolvisuals.animation.network;

import java.util.*;

public final class EmoteGate {
    private final Map<UUID,Long> last=new HashMap<>();
    public boolean start(UUID player,long tick) {
        var previous=last.get(player);if(previous==null&&last.size()>=RelayLimits.MAX_PLAYERS||previous!=null&&tick>=previous&&tick-previous<10)return false;last.put(player,tick);return true;
    }
    public void retain(Collection<UUID> players){last.keySet().retainAll(players);}
    public void remove(UUID player){last.remove(player);}
}

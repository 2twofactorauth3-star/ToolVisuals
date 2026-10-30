package ru.he3hauka.toolvisuals.animation.protocol.server;

import java.util.*;

import ru.he3hauka.toolvisuals.animation.network.PacketBudget;

public final class ObserverAdmissions {
    public static final long UNSUPPORTED_NANOS=120_000_000_000L,RETRY_NANOS=1_000_000_000L;
    private final Set<UUID> pending=new HashSet<>();
    private final Map<UUID,Long> retryAfter=new LinkedHashMap<>();
    private final PacketBudget budget;
    public ObserverAdmissions(){this(System.nanoTime());}
    public ObserverAdmissions(long now){budget=new PacketBudget(64,64,now);}
    public synchronized boolean acquire(UUID actor,long now){
        var retry=retryAfter.get(actor);
        if(pending.size()>=256||pending.contains(actor)||retry!=null&&now-retry<0||!budget.allow(now))return false;
        pending.add(actor);return true;
    }
    public synchronized void release(UUID actor){pending.remove(actor);}
    public synchronized void finish(UUID actor,long now,long delay){
        pending.remove(actor);retryAfter.remove(actor);
        if(delay<=0)return;
        retryAfter.put(actor,now+delay);
        if(retryAfter.size()>4096)retryAfter.remove(retryAfter.keySet().iterator().next());
    }
    public synchronized void forget(UUID actor){retryAfter.remove(actor);}
    public synchronized int size(){return pending.size();}
}

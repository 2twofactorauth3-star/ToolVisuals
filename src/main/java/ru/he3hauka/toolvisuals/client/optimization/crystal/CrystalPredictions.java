package ru.he3hauka.toolvisuals.client.optimization.crystal;

import java.util.*;

public final class CrystalPredictions<T> {
    public static final int LIMIT=64;
    private record Pending<T>(T entity,long deadline){}
    private final Map<Integer,Pending<T>> pending=new LinkedHashMap<>();
    public boolean begin(int id,T entity,long now,long timeout){
        if(entity==null||timeout<1||pending.size()>=LIMIT||pending.containsKey(id))return false;
        pending.put(id,new Pending<>(entity,now+Math.min(timeout,3_000_000_000L)));return true;
    }
    public void confirmed(int id){pending.remove(id);}
    public List<T> expired(long now){
        var ready=new ArrayList<T>();var iterator=pending.values().iterator();while(iterator.hasNext()){var entry=iterator.next();if(now-entry.deadline>=0){ready.add(entry.entity);iterator.remove();}}return List.copyOf(ready);
    }
    public List<T> drain(){var ready=pending.values().stream().map(Pending::entity).toList();pending.clear();return ready;}
    public void clear(){pending.clear();}
    public int size(){return pending.size();}
    public static long timeout(double seconds,int ping){return (long)(Math.clamp(Math.max(Double.isFinite(seconds)?seconds:1,Math.max(0,ping)*.003+.25),.3,3)*1_000_000_000L);}
}

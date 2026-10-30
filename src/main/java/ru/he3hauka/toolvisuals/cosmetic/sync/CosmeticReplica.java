package ru.he3hauka.toolvisuals.cosmetic.sync;

import java.util.*;

public final class CosmeticReplica<S extends CosmeticState<?>> {
    public record Result(boolean accepted,UUID evicted){}
    private final int maximum;
    private final LinkedHashMap<UUID,S> states=new LinkedHashMap<>();
    private final Set<UUID> unloaded=new HashSet<>();
    public CosmeticReplica(int maximum){if(maximum<1)throw new IllegalArgumentException();this.maximum=maximum;}
    public Result accept(UUID server,S state){
        if(!state.server().equals(server))return new Result(false,null);
        var old=states.get(state.actor());
        if(old!=null&&(state.epoch()<old.epoch()||state.epoch()==old.epoch()&&(state.revision()<old.revision()||state.revision()==old.revision()&&!unloaded.contains(state.actor()))))return new Result(false,null);
        states.put(state.actor(),state);unloaded.remove(state.actor());UUID evicted=null;
        if(states.size()>maximum){evicted=states.keySet().iterator().next();states.remove(evicted);unloaded.remove(evicted);}
        return new Result(true,evicted);
    }
    public S state(UUID actor){return unloaded.contains(actor)?null:states.get(actor);}
    public void unload(UUID actor){if(states.containsKey(actor))unloaded.add(actor);}
    public void clear(){states.clear();unloaded.clear();}
    public int size(){return states.size();}
}

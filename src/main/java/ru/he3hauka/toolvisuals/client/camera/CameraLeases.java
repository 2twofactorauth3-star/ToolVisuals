package ru.he3hauka.toolvisuals.client.camera;

import java.util.EnumMap;
import java.util.Objects;

public final class CameraLeases<T> {
    public enum Owner { EMOTE, FREE_LOOK, HAND_EDITOR }
    private final EnumMap<Owner,T> leases=new EnumMap<>(Owner.class);
    private T base,applied;
    public T base(T actual){observe(actual);return leases.isEmpty()?actual:base;}
    private void observe(T actual){if(!leases.isEmpty()&&!Objects.equals(actual,applied))base=actual;}
    public T acquire(Owner owner,T desired,T actual){
        observe(actual);
        if(leases.isEmpty())base=actual;
        leases.put(owner,desired);
        return resolve();
    }
    public T release(Owner owner,T actual){
        observe(actual);
        if(!leases.containsKey(owner))return actual;
        leases.remove(owner);
        if(leases.isEmpty()){T result=base;base=applied=null;return result;}
        return resolve();
    }
    public T update(T actual){observe(actual);return leases.isEmpty()?actual:resolve();}
    private T resolve(){applied=leases.containsKey(Owner.HAND_EDITOR)?leases.get(Owner.HAND_EDITOR):leases.containsKey(Owner.FREE_LOOK)?leases.get(Owner.FREE_LOOK):leases.get(Owner.EMOTE);return applied;}
}

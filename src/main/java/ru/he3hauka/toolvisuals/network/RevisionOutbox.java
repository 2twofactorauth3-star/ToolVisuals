package ru.he3hauka.toolvisuals.network;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public class RevisionOutbox<T> {
    public record Identity(UUID actor,long epoch,long revision){}
    private final Function<T,Identity> identity;
    private final Predicate<T> terminal;
    private final class Slot {
        T value;
        boolean pending=true;
        long sent;
        Slot(T value){this.value=value;}
    }
    private final int capacity;
    private final LinkedHashMap<UUID,Slot> slots=new LinkedHashMap<>();
    private boolean closed;
    public RevisionOutbox(int capacity,Function<T,Identity> identity,Predicate<T> terminal){if(capacity<1)throw new IllegalArgumentException();this.capacity=capacity;this.identity=identity;this.terminal=terminal;}
    public synchronized boolean offer(T value,long now){
        if(closed)return false;
        prune(now);
        var state=identity.apply(value);var old=slots.get(state.actor());
        if(old!=null){
            var previous=identity.apply(old.value);
            if(state.epoch()<previous.epoch()||state.epoch()==previous.epoch()&&state.revision()<=previous.revision())return true;
            old.value=value;old.pending=true;return true;
        }
        if(slots.size()>=capacity)return false;
        slots.put(state.actor(),new Slot(value));return true;
    }
    public synchronized T next(){
        for(var slot:slots.values())if(slot.pending&&terminal.test(slot.value))return slot.value;
        for(var slot:slots.values())if(slot.pending)return slot.value;
        return null;
    }
    public synchronized void sent(T value,long now){
        var key=identity.apply(value).actor();var slot=slots.get(key);
        if(slot==null||slot.value!=value)return;
        slots.remove(key);slot.pending=false;slot.sent=now;slots.put(key,slot);
    }
    public synchronized boolean contains(UUID actor){return slots.containsKey(actor);}
    public synchronized void discard(T value){var actor=identity.apply(value).actor();var slot=slots.get(actor);if(slot!=null&&slot.value==value)slots.remove(actor);}
    public synchronized void forget(UUID actor){slots.remove(actor);}
    public synchronized void replay(){for(var slot:slots.values())if(!terminal.test(slot.value))slot.pending=true;}
    public synchronized void prune(long now){slots.values().removeIf(slot->!slot.pending&&terminal.test(slot.value)&&now-slot.sent>=10_000_000_000L);}
    public synchronized int size(){return slots.size();}
    public synchronized void close(){closed=true;slots.clear();}
}

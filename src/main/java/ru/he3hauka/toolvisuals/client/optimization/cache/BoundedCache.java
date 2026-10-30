package ru.he3hauka.toolvisuals.client.optimization.cache;

import java.util.LinkedHashMap;

public final class BoundedCache<K,V> {
    private final LinkedHashMap<K,V> entries = new LinkedHashMap<>(64,.75f,true);
    private final int capacity;
    public BoundedCache(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Cache capacity must be positive");
        this.capacity = capacity;
    }
    public synchronized V get(K key) { return entries.get(key); }
    public synchronized void put(K key,V value) {
        entries.put(key,value);
        if (entries.size() > capacity) entries.remove(entries.keySet().iterator().next());
    }
    public synchronized void clear() { entries.clear(); }
    public synchronized int size() { return entries.size(); }
}

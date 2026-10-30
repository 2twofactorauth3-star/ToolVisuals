package ru.he3hauka.toolvisuals.client.utility.preview;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LatestWorkQueue<K,V> {
    private final int capacity;
    private final LinkedHashMap<K,V> waiting=new LinkedHashMap<>();
    public LatestWorkQueue(int capacity){if(capacity<1)throw new IllegalArgumentException();this.capacity=capacity;}
    public void offer(K key,V value){waiting.remove(key);waiting.put(key,value);if(waiting.size()>capacity)waiting.remove(waiting.keySet().iterator().next());}
    public Map.Entry<K,V> poll(){if(waiting.isEmpty())return null;var entry=waiting.lastEntry();waiting.remove(entry.getKey());return entry;}
    public boolean contains(K key){return waiting.containsKey(key);}
    public int size(){return waiting.size();}
    public void clear(){waiting.clear();}
}

package ru.he3hauka.toolvisuals.client.optimization.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.lang.ref.WeakReference;
import java.util.Objects;

public final class TickPositionCache<T> {
    private final int capacity;
    private final Long2ObjectOpenHashMap<T> entries;
    private WeakReference<Object> world = new WeakReference<>(null);
    private long tick = Long.MIN_VALUE;
    private long revision = Long.MIN_VALUE;

    public TickPositionCache(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
        entries = new Long2ObjectOpenHashMap<>(Math.min(capacity, 256));
    }

    public void prepare(Object currentWorld, long currentTick, long currentRevision) {
        boolean worldChanged = world.get() != currentWorld;
        if (!worldChanged && tick == currentTick && revision == currentRevision) return;
        entries.clear();
        tick = currentTick;
        revision = currentRevision;
        if (worldChanged) world = new WeakReference<>(currentWorld);
    }

    public T get(long position) {
        return entries.get(position);
    }

    public void put(long position, T value) {
        Objects.requireNonNull(value);
        if (entries.size() < capacity || entries.containsKey(position)) entries.put(position, value);
    }
}

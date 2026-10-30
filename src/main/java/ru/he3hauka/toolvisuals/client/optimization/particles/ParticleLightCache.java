package ru.he3hauka.toolvisuals.client.optimization.particles;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.lang.ref.WeakReference;

public final class ParticleLightCache {
    public static final int MISSING = Integer.MIN_VALUE;
    public static final int CAPACITY = 4096;
    private static final ThreadLocal<ParticleLightCache> CACHES = ThreadLocal.withInitial(ParticleLightCache::new);
    private final Long2IntOpenHashMap light = new Long2IntOpenHashMap(256);
    private WeakReference<Object> world = new WeakReference<>(null);
    private long tick = Long.MIN_VALUE;

    public ParticleLightCache() {
        light.defaultReturnValue(MISSING);
    }

    public static ParticleLightCache current(Object world, long tick) {
        var cache = CACHES.get();
        cache.prepare(world, tick);
        return cache;
    }

    public void prepare(Object currentWorld, long currentTick) {
        boolean worldChanged = world.get() != currentWorld;
        if (!worldChanged && tick == currentTick) return;
        light.clear();
        tick = currentTick;
        if (worldChanged) world = new WeakReference<>(currentWorld);
    }

    public int get(long position) {
        return light.get(position);
    }

    public void put(long position, int packedLight) {
        if (light.size() < CAPACITY) light.put(position, packedLight);
    }
}

package ru.he3hauka.toolvisuals.client.optimization.particles;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.world.level.material.FluidState;
import ru.he3hauka.toolvisuals.client.optimization.cache.TickPositionCache;

public final class ParticleFluids {
    public static final int CAPACITY = 1024;
    private static final AtomicLong REVISION = new AtomicLong();
    private static final ThreadLocal<TickPositionCache<FluidState>> CACHES =
            ThreadLocal.withInitial(() -> new TickPositionCache<>(CAPACITY));

    private ParticleFluids() {}

    public static TickPositionCache<FluidState> current(Object world, long tick) {
        var cache = CACHES.get();
        cache.prepare(world, tick, REVISION.get());
        return cache;
    }

    public static void invalidate() {
        REVISION.incrementAndGet();
    }
}

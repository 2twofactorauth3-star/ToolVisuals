package ru.he3hauka.toolvisuals.client.chunkanimator;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class ChunkAnimationTimes {
    private static final class Active {
        public final int time;
        public final long expires;
        Active(int time, long expires) { this.time = time; this.expires = expires; }
    }
    private final Long2ObjectOpenHashMap<Active> active = new Long2ObjectOpenHashMap<>();
    private long nextPrune;

    public int time(int region, int section, int proposed, long nowNanos, long durationNanos) {
        if (nowNanos >= nextPrune) {
            active.values().removeIf(entry -> nowNanos >= entry.expires);
            nextPrune = nowNanos + 1_000_000_000L;
        }
        long key = (long)region << 8 | section;
        if (proposed >= 0) {
            active.put(key, new Active(proposed, nowNanos + durationNanos));
            return proposed;
        }
        var previous = active.get(key);
        if (previous != null && nowNanos < previous.expires) return previous.time;
        active.remove(key); return proposed;
    }
    public void clearRegion(int region) { for (int i = 0; i < 256; i++) active.remove((long)region << 8 | i); }
    public void clear() { if (!active.isEmpty()) active.clear(); }
}

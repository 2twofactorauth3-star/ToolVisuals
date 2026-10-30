 
 
package ru.he3hauka.toolvisuals.client.optimization.text;

import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.client.gui.Font;

import ru.he3hauka.toolvisuals.client.optimization.cache.BoundedCache;

public final class PreparedTextCache {
    public record Key(StableText text,float x,float y,int color,boolean shadow,boolean includeEmpty,int background) {}
    private static final AtomicLong GENERATION = new AtomicLong();
    private final BoundedCache<Key,Font.PreparedText> entries = new BoundedCache<>(2048);
    private long generation = -1;
    public static void invalidate() { GENERATION.incrementAndGet(); }
    public synchronized Font.PreparedText get(Key key) {
        long current = GENERATION.get();
        if (generation != current) { entries.clear(); generation = current; }
        return entries.get(key);
    }
    public synchronized void put(Key key,Font.PreparedText value) { entries.put(key,value); }
}

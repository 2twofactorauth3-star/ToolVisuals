package ru.he3hauka.toolvisuals.client.optimization.text;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntSupplier;
import ru.he3hauka.toolvisuals.client.optimization.cache.BoundedCache;

public final class PreparedTextCache {
    private static final AtomicLong GENERATION=new AtomicLong();
    private final BoundedCache<String,Integer> widths=new BoundedCache<>(2048);
    private long generation=-1;
    public static void invalidate(){GENERATION.incrementAndGet();}
    public synchronized int width(String text,IntSupplier measure){long current=GENERATION.get();if(current!=generation){widths.clear();generation=current;}Integer width=widths.get(text);if(width==null){width=measure.getAsInt();widths.put(text,width);}return width;}
}

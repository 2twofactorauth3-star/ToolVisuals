package ru.he3hauka.toolvisuals.client.optimization.terrain;

public final class ChunkBudget {
    private ChunkBudget() {}
    public static long upload(long nativeBudget,long frameNanos) {
        long budget=Math.clamp(frameNanos/5,1_000_000L,3_000_000L);
        return Math.min(nativeBudget,budget);
    }
    public static long background(long nativeBudget,long frameNanos,int processors) {
        long frame=Math.clamp(frameNanos,1_000_000L,50_000_000L);
        long workers=Math.clamp(processors-1,1,4);
        return Math.min(nativeBudget,Math.max(1_000_000L,frame*workers/2));
    }
}

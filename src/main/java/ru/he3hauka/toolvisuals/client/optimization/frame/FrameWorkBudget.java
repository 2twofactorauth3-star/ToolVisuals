package ru.he3hauka.toolvisuals.client.optimization.frame;

public final class FrameWorkBudget {
    private long frame, limit, spent, lastTexture;

    public void begin(long frameNanos) {
        frame++;
        limit = Math.clamp(frameNanos / 4, 250_000L, 3_000_000L);
        spent = 0;
    }

    public long remaining() { return Math.max(0, limit - spent); }

    public long chunks(long ceiling) { return Math.min(Math.max(0, ceiling), remaining()); }

    public long textures(long ceiling) {
        long available = Math.min(Math.max(0, ceiling), remaining());
        return available > 0 ? available : frame - lastTexture >= 4 && ceiling > 0 ? 1 : 0;
    }

    public void consumed(long nanos, boolean texture) {
        spent += Math.clamp(nanos, 0, 1_000_000_000L);
        if (texture) lastTexture = frame;
    }
}

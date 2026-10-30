package ru.he3hauka.toolvisuals.client.animation;

public final class EmoteClock {
    private long previous;
    private double ticks;
    private boolean initialized, paused;

    public void reset(long now) {
        previous = now;
        ticks = 0;
        initialized = true;
        paused = false;
    }

    public double sample(long now, boolean pause) {
        if (!initialized) reset(now);
        long elapsed = now - previous;
        if (elapsed > 0) {
            if (!pause && !paused) ticks += elapsed / 50_000_000d;
            previous = now;
        }
        paused = pause;
        return ticks;
    }
}

package ru.he3hauka.toolvisuals.client.input.tape;

public final class TapePlayback {
    public static final double MAX_INTERVAL_SECONDS = 60;
    private int keys;
    private int down;
    private int pending;
    private long interval;
    private long lastPulse;
    private boolean running;

    public void tick(TapeAction action, double seconds, long now, boolean available) {
        long nextInterval = intervalNanos(seconds);
        boolean changed = keys != action.mask || interval != nextInterval;
        keys = action.mask;
        interval = nextInterval;
        down = pending = 0;
        if (!available) { running = false; return; }
        boolean due = !running || changed || interval > 0 && now - lastPulse >= interval;
        if (interval == 0 || due) down = keys;
        if (due) {
            pending = keys & TapeAction.BOTH.mask;
            lastPulse = now;
        }
        running = true;
    }

    public boolean down(int button) { return (down & button) != 0; }

    public boolean consumeClick(int button, boolean physical) {
        boolean click = (pending & button) != 0;
        pending &= ~button;
        return physical || click;
    }

    public void clear() { down = pending = keys = 0; running = false; }

    private static long intervalNanos(double seconds) {
        return Double.isFinite(seconds) ? Math.round(Math.clamp(seconds, 0, MAX_INTERVAL_SECONDS) * 1_000_000_000) : 0;
    }
}

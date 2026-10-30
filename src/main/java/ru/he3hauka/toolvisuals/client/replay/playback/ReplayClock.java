package ru.he3hauka.toolvisuals.client.replay.playback;

public final class ReplayClock {
    private long previous;
    private double positionMillis, speed = 1;
    private boolean initialized, playing;
    public void play(boolean playing, long now) { advance(now); this.playing = playing; }
    public void speed(double speed, long now) {
        if (!Double.isFinite(speed) || speed < .1 || speed > 8) throw new IllegalArgumentException("Invalid replay speed");
        advance(now); this.speed = speed;
    }
    public void seek(long millis, long now) {
        if (millis < 0) throw new IllegalArgumentException("Invalid replay position");
        positionMillis = millis; previous = now; initialized = true;
    }
    public double advance(long now) {
        if (initialized && playing && now > previous) positionMillis += (now - previous) / 1000000d * speed;
        previous = initialized ? Math.max(previous, now) : now; initialized = true;
        return positionMillis;
    }
    public boolean playing() { return playing; }
    public double speed() { return speed; }
}

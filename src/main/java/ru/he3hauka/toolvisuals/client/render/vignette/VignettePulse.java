package ru.he3hauka.toolvisuals.client.render.vignette;

public final class VignettePulse {
    private long started;
    private boolean running;

    public void trigger(long now) { started=now;running=true; }
    public void clear() { running=false; }

    public double value(long now,double duration) {
        if (!running || now<started || !Double.isFinite(duration) || duration<=0) return 0;
        double age=(now-started)/1_000_000_000d/duration;
        if (age>=1) { running=false;return 0; }
        return 1-age*age*(3-2*age);
    }
}

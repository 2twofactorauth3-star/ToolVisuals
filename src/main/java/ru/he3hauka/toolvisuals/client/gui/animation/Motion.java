package ru.he3hauka.toolvisuals.client.gui.animation;

public final class Motion {
    private float value;

    public Motion(float initialValue) {
        value = initialValue;
    }

    public float value() {
        return value;
    }

    public void snap(float next) {
        value = next;
    }

    public float step(float target, float elapsedSeconds, float response) {
        if (elapsedSeconds <= 0) return value;
        float blend = (float) -Math.expm1(-response * elapsedSeconds);
        value += (target - value) * blend;
        if (Math.abs(target - value) < 0.0001f) value = target;
        return value;
    }
}

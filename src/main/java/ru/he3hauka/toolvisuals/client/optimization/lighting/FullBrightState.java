package ru.he3hauka.toolvisuals.client.optimization.lighting;

import java.lang.ref.WeakReference;

public final class FullBrightState {
    private WeakReference<Object> world = new WeakReference<>(null);
    private boolean initialized;
    private boolean enabled;
    private float brightness;

    public boolean update(Object currentWorld, boolean currentEnabled, float currentBrightness) {
        boolean worldChanged = world.get() != currentWorld;
        boolean changed = !initialized || worldChanged || enabled != currentEnabled
                || currentEnabled && Float.compare(brightness, currentBrightness) != 0;
        if (worldChanged) world = new WeakReference<>(currentWorld);
        initialized = true;
        enabled = currentEnabled;
        brightness = currentBrightness;
        return changed;
    }
}

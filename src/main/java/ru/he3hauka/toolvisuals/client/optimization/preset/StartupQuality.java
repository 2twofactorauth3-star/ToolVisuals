package ru.he3hauka.toolvisuals.client.optimization.preset;

import net.minecraft.client.Options;

public final class StartupQuality {
    private StartupQuality() {
    }

    public static void apply(Options options) {
        options.graphicsPreset().set(net.minecraft.client.GraphicsPreset.FAST);
        options.entityDistanceScaling().set(1d);
        options.cutoutLeaves().set(false);
        options.mipmapLevels().set(2);
    }
}

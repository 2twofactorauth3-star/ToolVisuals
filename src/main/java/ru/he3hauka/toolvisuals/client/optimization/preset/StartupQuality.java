package ru.he3hauka.toolvisuals.client.optimization.preset;

import net.minecraft.client.Options;

public final class StartupQuality {
    private StartupQuality() {
    }

    public static void apply(Options options) {
        options.cutoutLeaves().set(false);
    }
}

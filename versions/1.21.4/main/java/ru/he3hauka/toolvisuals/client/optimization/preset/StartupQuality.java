package ru.he3hauka.toolvisuals.client.optimization.preset;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptions.GraphicsQuality;
import net.minecraft.client.Options;

public final class StartupQuality {
    private StartupQuality() {
    }

    public static void apply(Options options) {
        options.graphicsMode().set(net.minecraft.client.GraphicsStatus.FAST);
        options.mipmapLevels().set(2);
        SodiumClientMod.options().quality.leavesQuality = GraphicsQuality.FAST;
        StartupLeafCulling.apply();
    }
}

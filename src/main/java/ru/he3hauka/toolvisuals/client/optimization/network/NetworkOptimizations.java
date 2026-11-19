package ru.he3hauka.toolvisuals.client.optimization.network;

import net.fabricmc.loader.api.FabricLoader;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;

public final class NetworkOptimizations {
    private static final boolean EXTERNAL=FabricLoader.getInstance().isModLoaded("krypton");
    private NetworkOptimizations() {}
    public static boolean enabled(){return !EXTERNAL&&Optimizations.enabled(VisualSetting.OPT_NETWORK);}
}

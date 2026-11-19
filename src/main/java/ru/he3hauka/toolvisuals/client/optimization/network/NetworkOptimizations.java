package ru.he3hauka.toolvisuals.client.optimization.network;

import net.fabricmc.loader.api.FabricLoader;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class NetworkOptimizations {
    private static final boolean EXTERNAL=FabricLoader.getInstance().isModLoaded("krypton");
    private static volatile boolean enabled;
    private NetworkOptimizations() {}
    public static boolean external(){return EXTERNAL;}
    public static boolean enabled(){return enabled;}
    public static void update(ClientConfig config){enabled=!EXTERNAL&&config.active(VisualModule.KRYPTON_OPTIMIZATION);}
}

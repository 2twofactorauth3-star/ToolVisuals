package ru.he3hauka.toolvisuals.client.compatibility.input;

import me.decce.ixeris.api.IxerisApi;
import net.fabricmc.loader.api.FabricLoader;

public final class NativeEventThread {
    private static final boolean IXERIS = FabricLoader.getInstance().isModLoaded("ixeris");

    private NativeEventThread() {}

    public static void execute(Runnable action) {
        if (IXERIS) IxerisApi.getInstance().runOnMainThread(action);
        else action.run();
    }
}

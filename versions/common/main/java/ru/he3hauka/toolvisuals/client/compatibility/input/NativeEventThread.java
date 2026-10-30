package ru.he3hauka.toolvisuals.client.compatibility.input;

import net.minecraft.client.Minecraft;

public final class NativeEventThread {
    private NativeEventThread() {}
    public static void execute(Runnable action) {
        var client=Minecraft.getInstance();if(client.isSameThread())action.run();else client.execute(action);
    }
}

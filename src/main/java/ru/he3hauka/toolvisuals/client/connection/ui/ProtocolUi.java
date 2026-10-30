package ru.he3hauka.toolvisuals.client.connection.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ProtocolUi {
    private ProtocolUi() {}

    public static void open(Object parent) {
        Minecraft.getInstance().gui.setScreen(new ConnectionScreen((Screen) parent, true));
    }
}

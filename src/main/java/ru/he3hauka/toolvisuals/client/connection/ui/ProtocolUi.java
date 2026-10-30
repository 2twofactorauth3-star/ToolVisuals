package ru.he3hauka.toolvisuals.client.connection.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;

public final class ProtocolUi {
    private ProtocolUi() {}

    public static void open(Object parent) {
        var client = Minecraft.getInstance();
        if (client.level != null || client.getConnection() != null) {
            ChatMessages.notice("connection.disconnect_first");
            return;
        }
        client.gui.setScreen(new ConnectionScreen((Screen) parent, true));
    }
}

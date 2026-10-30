package ru.he3hauka.toolvisuals.relay.paper.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public final class ServerMessages {
    private ServerMessages() {}

    public static Component text(String message) {
        return Component.text("[ToolVisuals] ", TextColor.color(0xb59aff)).append(Component.text(message, TextColor.color(0xd5cbea)));
    }
}

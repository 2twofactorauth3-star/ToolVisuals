package ru.he3hauka.toolvisuals.client.chat.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class ChatMessages {
    private ChatMessages() {}

    public static MutableComponent translated(String key, Object... arguments) {
        Object[] values = new Object[arguments.length];
        for (int index = 0; index < values.length; index++) {
            Object value = arguments[index];
            if (value instanceof Component component) {
                var copy = component.copy();
                values[index] = copy.getStyle().getColor() == null ? copy.withColor(ChatColors.VALUE) : copy;
            } else values[index] = value instanceof String text ? Component.literal(text).withColor(ChatColors.VALUE) : value;
        }
        return message(UiI18n.tr("toolvisuals." + key, values));
    }

    public static MutableComponent message(Component body) {
        return Component.empty().withColor(ChatColors.TEXT)
                .append(Component.literal("[ToolVisuals] ").withColor(ChatColors.ACCENT)).append(body);
    }

    public static void notice(String key, Object... arguments) {
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.event("Notice",key,"argument_count="+arguments.length);
        send(translated(key, arguments));
    }

    public static void send(Component message) {
        var client = Minecraft.getInstance();
        if (client.gui != null && client.gui.hud != null) client.gui.hud.getChat().addClientSystemMessage(message);
    }
}

package ru.he3hauka.toolvisuals.client.menu;

import java.util.List;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class TitleMenuButtons {
    private TitleMenuButtons() {}

    public static Button find(List<Button> buttons, String key) {
        if (key == null) return null;
        String label = Component.translatable(key).getString();
        return buttons.stream().filter(button -> button.getMessage().getString().equals(label)).findFirst().orElse(null);
    }
}

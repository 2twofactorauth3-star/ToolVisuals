package ru.he3hauka.toolvisuals.client.module;

import java.util.Locale;

import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public enum ModuleCategory {
    FAVORITES, WORLD, PLAYER, RENDER, HUD, FRIENDS, CONFIGS;

    public final String id = name().toLowerCase(Locale.ROOT);

    public Component label() {
        return UiI18n.tr("toolvisuals.modules.category." + id);
    }

    public Component description() {
        return UiI18n.tr("toolvisuals.modules.category." + id + ".description");
    }
}

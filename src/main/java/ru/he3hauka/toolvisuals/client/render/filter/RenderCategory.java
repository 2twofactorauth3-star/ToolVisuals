package ru.he3hauka.toolvisuals.client.render.filter;

import java.util.Locale;

import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public enum RenderCategory {
    WORLD, ENTITIES, EFFECTS, CAMERA, HUD;

    public final String id = name().toLowerCase(Locale.ROOT);

    public Component label() {
        return UiI18n.tr("toolvisuals.category." + id);
    }
}


package ru.he3hauka.toolvisuals.client.menu.multiplayer;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import ru.he3hauka.toolvisuals.client.connection.ConnectionVersions;
import ru.he3hauka.toolvisuals.client.connection.ui.ProtocolUi;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class MultiplayerVersionsButton {
    private static final Set<Button> BUTTONS = Collections.newSetFromMap(new WeakHashMap<>());

    private MultiplayerVersionsButton() {}

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register(MultiplayerVersionsButton::add);
    }

    private static void add(Minecraft client, Screen screen, int width, int height) {
        if (!(screen instanceof JoinMultiplayerScreen)) return;
        var buttons = Screens.getWidgets(screen);
        buttons.removeIf(BUTTONS::remove);
        var button = Button.builder(UiI18n.tr("toolvisuals.main.versions"), unused -> ProtocolUi.open(screen))
                .bounds(5, 5, 100, 20)
                .tooltip(Tooltip.create(UiI18n.tr("toolvisuals.connection.version_value", ConnectionVersions.label())))
                .build();
        button.active = client.level == null && client.getConnection() == null;
        BUTTONS.add(button);
        buttons.add(button);
    }
}

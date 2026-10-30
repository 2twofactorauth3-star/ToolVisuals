package ru.he3hauka.toolvisuals.client.localization;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

public final class SodiumLanguage {
    private static Language previous, overlay;
    private SodiumLanguage() { }

    public static void screenChanged(Screen screen) {
        boolean sodium = screen != null && screen.getClass().getName().startsWith("net.caffeinemc.mods.sodium.client.gui.");
        if (sodium) {
            if (overlay != null && Language.getInstance() == overlay) return;
            previous = Language.getInstance();
            String id = ToolVisualsClient.CONFIG.language().id;
            overlay = ClientLanguage.loadFrom(Minecraft.getInstance().getResourceManager(),
                    id.equals("en_us") ? List.of("en_us") : List.of("en_us", id), false);
            Language.inject(overlay);
        } else {
            if (overlay != null && Language.getInstance() == overlay) Language.inject(previous);
            overlay = previous = null;
        }
    }
}

package com.viaversion.viafabricplus.visuals.features.force_unicode_font;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Language;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class UnicodeFontFix1_12_2 {

    private static boolean enabled = false;
    private static Runnable task = null;

    static {
        ViaFabricPlus.getImpl().registerOnChangeProtocolVersionCallback((oldVersion, newVersion) -> {
            updateUnicodeFontOverride(newVersion);
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {

            if (task != null) {
                task.run();
                task = null;
            }
        });
    }

    public static void init() {

    }

    public static void updateUnicodeFontOverride(final ProtocolVersion version) {
        final SimpleOption<Boolean> option = MinecraftClient.getInstance().options.getForceUnicodeFont();

        if (VisualSettings.INSTANCE.forceUnicodeFontForNonAsciiLanguages.isEnabled(version)) {
            if (Language.getInstance() instanceof TranslationStorage storage) {
                enabled = LanguageUtil.isUnicodeFont1_12_2(storage.translations);
                task = () -> option.setValue(enabled);
            }
        } else if (enabled) {
            enabled = false;
            task = () -> option.setValue(false);
        }
    }

}

package com.viaversion.viafabricplus.visuals.features.force_unicode_font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class UnicodeFontFix1_12_2 {

    private static boolean enabled = false;
    private static Runnable task = null;

    public static void init() {
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

    public static void updateUnicodeFontOverride(final ProtocolVersion version) {
        final OptionInstance<Boolean> option = Minecraft.getInstance().options.forceUnicodeFont();

        if (VisualSettings.INSTANCE.forceUnicodeFontForNonAsciiLanguages.isEnabled(version)) {
            if (Language.getInstance() instanceof ClientLanguage storage) {
                enabled = LanguageUtil.isUnicodeFont1_12_2(storage.storage);
                task = () -> option.set(enabled);
            }
        } else if (enabled) {
            enabled = false;
            task = () -> option.set(false);
        }
    }

}

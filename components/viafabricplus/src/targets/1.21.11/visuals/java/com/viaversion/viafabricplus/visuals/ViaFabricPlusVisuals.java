package com.viaversion.viafabricplus.visuals;

import net.minecraft.client.Minecraft;

import com.viaversion.viafabricplus.api.ViaFabricPlusBase;
import com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusLoadEntrypoint;
import com.viaversion.viafabricplus.api.events.LoadingCycleCallback;
import com.viaversion.viafabricplus.visuals.features.classic.creative_menu.GridItemSelectionScreen;
import com.viaversion.viafabricplus.visuals.features.force_unicode_font.UnicodeFontFix1_12_2;
import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;

public class ViaFabricPlusVisuals implements ViaFabricPlusLoadEntrypoint {

    public static final ViaFabricPlusVisuals INSTANCE = new ViaFabricPlusVisuals();

    @Override
    public void onPlatformLoad(ViaFabricPlusBase platform) {
        UnicodeFontFix1_12_2.init();

        platform.registerLoadingCycleCallback(cycle -> {
            if (cycle == LoadingCycleCallback.LoadingCycle.POST_SETTINGS_LOAD) {
                platform.addSettingGroup(VisualSettings.INSTANCE);
            }
        });

        platform.registerOnChangeProtocolVersionCallback((oldVersion, newVersion) -> Minecraft.getInstance().execute(() -> {
            if (newVersion.olderThanOrEqualTo(LegacyProtocolVersion.c0_28toc0_30)) {
                GridItemSelectionScreen.INSTANCE.itemGrid = null;
            }
        }));
    }

}

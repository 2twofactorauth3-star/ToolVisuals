package com.viaversion.viafabricplus.visuals.injection.mixin.downloading_terrain_transitions;

import net.minecraft.client.gui.screen.DownloadingTerrainScreen;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DownloadingTerrainScreen.class)
public abstract class MixinDownloadingTerrainScreen {

    @Shadow
    @Final
    private DownloadingTerrainScreen.WorldEntryReason worldEntryReason;

    @Redirect(method = "renderBackground", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen;worldEntryReason:Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;"))
    private DownloadingTerrainScreen.WorldEntryReason hideDownloadTerrainScreenTransitionEffects(DownloadingTerrainScreen downloadingTerrainScreen) {
        if (VisualSettings.INSTANCE.hideDownloadTerrainScreenTransitionEffects.isEnabled()) {
            return DownloadingTerrainScreen.WorldEntryReason.OTHER;
        } else {
            return this.worldEntryReason;
        }
    }

}

package com.viaversion.viafabricplus.visuals.injection.mixin.level_loading_transitions;

import net.minecraft.client.gui.screens.LevelLoadingScreen;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelLoadingScreen.class)
public abstract class MixinLevelLoadingScreen {

    @Shadow
    private LevelLoadingScreen.Reason reason;

    @Redirect(method = "renderBackground", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/LevelLoadingScreen;reason:Lnet/minecraft/client/gui/screens/LevelLoadingScreen$Reason;"))
    private LevelLoadingScreen.Reason hideDownloadTerrainScreenTransitionEffects(LevelLoadingScreen levelLoadingScreen) {
        if (VisualSettings.INSTANCE.hideDownloadTerrainScreenTransitionEffects.isEnabled()) {
            return LevelLoadingScreen.Reason.OTHER;
        } else {
            return this.reason;
        }
    }

}

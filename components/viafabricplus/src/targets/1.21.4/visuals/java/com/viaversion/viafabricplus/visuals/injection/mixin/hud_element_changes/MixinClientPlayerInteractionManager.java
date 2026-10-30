package com.viaversion.viafabricplus.visuals.injection.mixin.hud_element_changes;

import net.minecraft.client.network.ClientPlayerInteractionManager;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {

    @Inject(method = "hasExperienceBar", at = @At("HEAD"), cancellable = true)
    private void removeExperienceBar(CallbackInfoReturnable<Boolean> cir) {
        if (VisualSettings.INSTANCE.hideModernHUDElements.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

}

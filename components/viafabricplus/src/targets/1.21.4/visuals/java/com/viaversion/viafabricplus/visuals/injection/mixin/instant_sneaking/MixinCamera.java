package com.viaversion.viafabricplus.visuals.injection.mixin.instant_sneaking;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow
    private float cameraY;

    @Shadow
    private float lastCameraY;

    @Shadow
    private Entity focusedEntity;

    @Inject(method = "updateEyeHeight", at = @At(value = "HEAD"), cancellable = true)
    private void sneakInstantly(CallbackInfo ci) {
        if (this.focusedEntity != null && VisualSettings.INSTANCE.sneakInstantly.isEnabled()) {
            ci.cancel();
            cameraY = lastCameraY = focusedEntity.getStandingEyeHeight();
        }
    }

}

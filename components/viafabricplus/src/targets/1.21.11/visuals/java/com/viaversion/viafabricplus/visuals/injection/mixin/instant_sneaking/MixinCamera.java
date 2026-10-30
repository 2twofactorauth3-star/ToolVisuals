package com.viaversion.viafabricplus.visuals.injection.mixin.instant_sneaking;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private Entity entity;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeProbe;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.BEFORE))
    private void sneakInstantly(CallbackInfo ci) {
        if (VisualSettings.INSTANCE.sneakInstantly.isEnabled()) {
            eyeHeight = eyeHeightOld = entity.getEyeHeight();
        }
    }

}

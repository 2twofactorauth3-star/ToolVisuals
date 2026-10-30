package com.viaversion.viafabricplus.visuals.injection.mixin.player_rotations;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.math.MathHelper;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BipedEntityModel.class)
public abstract class MixinBipedEntityModel {

    @Redirect(method = "positionBlockingArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"))
    private float preventArmFollowingThirdPersonRotation(float value, float min, float max) {
        if (VisualSettings.INSTANCE.lockBlockingArmRotation.isEnabled()) {
            return 0.0F;
        } else {
            return MathHelper.clamp(value, min, max);
        }
    }

}

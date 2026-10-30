package com.viaversion.viafabricplus.visuals.injection.mixin.player_rotations;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel {

    @Redirect(method = "poseBlockingArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float preventArmFollowingThirdPersonRotation(float value, float min, float max) {
        if (VisualSettings.INSTANCE.lockBlockingArmRotation.isEnabled()) {
            return 0.0F;
        } else {
            return Mth.clamp(value, min, max);
        }
    }

}

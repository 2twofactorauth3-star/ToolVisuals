package com.viaversion.viafabricplus.visuals.injection.mixin.classic.walking_animation;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.math.MathHelper;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class MixinBipedEntityModel<T extends BipedEntityRenderState> {

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;roll:F", ordinal = 1, shift = At.Shift.AFTER))
    private void addOldWalkAnimation(T bipedEntityRenderState, CallbackInfo ci) {
        if (VisualSettings.INSTANCE.oldWalkingAnimation.isEnabled()) {
            final float limbFrequency = bipedEntityRenderState.limbFrequency;
            final float limbAmplitudeMultiplier = bipedEntityRenderState.limbAmplitudeMultiplier;

            this.rightArm.pitch = MathHelper.cos(limbFrequency * 0.6662F + 3.1415927F) * 2.0F * limbAmplitudeMultiplier;
            this.rightArm.roll = (MathHelper.cos(limbFrequency * 0.2312F) + 1.0F) * 1.0F * limbAmplitudeMultiplier;

            this.leftArm.pitch = MathHelper.cos(limbFrequency * 0.6662F) * 2.0F * limbAmplitudeMultiplier;
            this.leftArm.roll = (MathHelper.cos(limbFrequency * 0.2812F) - 1.0F) * 1.0F * limbAmplitudeMultiplier;
        }
    }

}

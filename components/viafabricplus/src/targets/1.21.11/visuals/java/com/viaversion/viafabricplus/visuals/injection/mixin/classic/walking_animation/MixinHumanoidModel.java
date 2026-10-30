package com.viaversion.viafabricplus.visuals.injection.mixin.classic.walking_animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel<T extends HumanoidRenderState> {

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/geom/ModelPart;zRot:F", ordinal = 1, shift = At.Shift.AFTER))
    private void addOldWalkAnimation(T bipedEntityRenderState, CallbackInfo ci) {
        if (VisualSettings.INSTANCE.oldWalkingAnimation.isEnabled()) {
            final float limbSwingAnimationProgress = bipedEntityRenderState.walkAnimationPos;
            final float limbSwingAmplitude = bipedEntityRenderState.walkAnimationSpeed;

            this.rightArm.xRot = Mth.cos(limbSwingAnimationProgress * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmplitude;
            this.rightArm.zRot = (Mth.cos(limbSwingAnimationProgress * 0.2312F) + 1.0F) * 1.0F * limbSwingAmplitude;

            this.leftArm.xRot = Mth.cos(limbSwingAnimationProgress * 0.6662F) * 2.0F * limbSwingAmplitude;
            this.leftArm.zRot = (Mth.cos(limbSwingAnimationProgress * 0.2812F) - 1.0F) * 1.0F * limbSwingAmplitude;
        }
    }

}

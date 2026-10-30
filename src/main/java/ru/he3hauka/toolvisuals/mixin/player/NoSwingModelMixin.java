package ru.he3hauka.toolvisuals.mixin.player;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.render.NoSwing;

@Mixin(HumanoidModel.class)
public abstract class NoSwingModelMixin {
    @Inject(method = "setupAttackAnimation", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$staticAttack(HumanoidRenderState state, CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatar && NoSwing.player(avatar)) ci.cancel();
    }
}

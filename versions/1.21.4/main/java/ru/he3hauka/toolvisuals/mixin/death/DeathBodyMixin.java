package ru.he3hauka.toolvisuals.mixin.death;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.animation.death.DeathAscension;
import ru.he3hauka.toolvisuals.client.animation.death.SoulRenderState;

@Mixin(LivingEntityRenderer.class)
public abstract class DeathBodyMixin {
    @Inject(method="render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$hideBody(CallbackInfo callback,@Local(argsOnly=true) LivingEntityRenderState state){if(state instanceof PlayerRenderState player&&!(state instanceof SoulRenderState)&&state.deathTime>0&&DeathAscension.hideBody(player.id))callback.cancel();}
}

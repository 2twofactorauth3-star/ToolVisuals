package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import ru.he3hauka.toolvisuals.client.optimization.occlusion.OccludedState;

@Mixin(LivingEntityRenderer.class)
public abstract class OccludedLivingMixin {
    @WrapOperation(method="render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType toolvisuals$skipBody(LivingEntityRenderer<?,?,?> renderer,LivingEntityRenderState state,boolean body,boolean translucent,boolean glowing,Operation<RenderType> original){
        return ((OccludedState)state).toolvisuals$occluded()?null:original.call(renderer,state,body,translucent,glowing);
    }
    @WrapOperation(method="render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z"))
    private boolean toolvisuals$skipLayers(LivingEntityRenderer<?,?,?> renderer,LivingEntityRenderState state,Operation<Boolean> original){
        return !((OccludedState)state).toolvisuals$occluded()&&original.call(renderer,state);
    }
}

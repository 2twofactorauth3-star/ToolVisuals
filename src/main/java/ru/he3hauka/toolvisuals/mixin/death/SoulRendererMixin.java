package ru.he3hauka.toolvisuals.mixin.death;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.animation.death.SoulRenderState;

@Mixin(LivingEntityRenderer.class)
public abstract class SoulRendererMixin {
    @Inject(method="getRenderType",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$soulType(LivingEntityRenderState state,boolean visible,boolean translucent,boolean glowing,CallbackInfoReturnable<RenderType> cir){
        if(state instanceof SoulRenderState soul)cir.setReturnValue(RenderTypes.entityTranslucentCull(soul.skin.body().texturePath()));
    }
    @Inject(method="getModelTint",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$soulTint(LivingEntityRenderState state,CallbackInfoReturnable<Integer> cir){if(state instanceof SoulRenderState soul)cir.setReturnValue(soul.tint);}
}

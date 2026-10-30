package ru.he3hauka.toolvisuals.mixin.motionblur;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MotionBlurMixin {
    @Inject(method="renderLevel",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V"))
    private void toolvisuals$blurBeforeHands(CallbackInfo callback){ru.he3hauka.toolvisuals.client.render.motionblur.MotionBlur.render((GameRenderer)(Object)this);}
}

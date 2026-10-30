package ru.he3hauka.toolvisuals.mixin.motionblur;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.motionblur.MotionBlur;

@Mixin(GameRenderer.class)
public abstract class MotionBlurMixin {
    @Inject(method="renderLevel",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V"))
    private void toolvisuals$motion(CallbackInfo callback){MotionBlur.render((GameRenderer)(Object)this);}
}

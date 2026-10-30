package ru.he3hauka.toolvisuals.mixin.motionblur;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.motionblur.MotionBlur;

@Mixin(GameRenderer.class)
public abstract class MotionBlurMixin {
    @Shadow @Final private GameRenderState gameRenderState;
    @Inject(method = "extract", at = @At("TAIL"))
    private void toolvisuals$motionEffect(CallbackInfo callback) { MotionBlur.requested(gameRenderState.requestedPostEffects); }
    @Inject(method = "applyPostEffects", at = @At("HEAD"))
    private void toolvisuals$motionHistory(CallbackInfo callback) { MotionBlur.prepare((GameRenderer)(Object)this); }
}

package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private GameRenderState gameRenderState;

    @Inject(method = "extract", at = @At("HEAD"))
    private void toolvisuals$beginWorkFrame(CallbackInfo ci) { ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork.begin(); }

    @Inject(method = "render", at = @At("HEAD"))
    private void toolvisuals$beginFrame(CallbackInfo ci) { ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor.begin(); }
    @Inject(method = "render", at = @At("RETURN"))
    private void toolvisuals$endFrame(CallbackInfo ci) { ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor.end(); }

    @Inject(method = "extract", at = @At("TAIL"))
    private void toolvisuals$hidePostEffects(CallbackInfo ci) {
        if (NoRender.hides(POST_EFFECTS)) gameRenderState.requestedPostEffects.clear();
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$bobHurt(CallbackInfo ci) {
        if (NoRender.hides(HURT_CAMERA)) ci.cancel();
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$bobView(CallbackInfo ci) {
        if (NoRender.hides(VIEW_BOBBING)) ci.cancel();
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$renderItemInHand(CallbackInfo ci) {
        if (NoRender.hides(FIRST_PERSON_HANDS)) ci.cancel();
    }
}


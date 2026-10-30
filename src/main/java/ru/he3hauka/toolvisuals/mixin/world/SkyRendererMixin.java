package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$renderSun(CallbackInfo ci) {
        if (NoRender.hides(SUN)) ci.cancel();
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$renderMoon(CallbackInfo ci) {
        if (NoRender.hides(MOON)) ci.cancel();
    }

    @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$renderSunriseAndSunset(CallbackInfo ci) {
        if (NoRender.hides(SUNRISE)) ci.cancel();
    }
}


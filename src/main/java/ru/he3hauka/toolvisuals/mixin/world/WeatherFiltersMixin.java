package ru.he3hauka.toolvisuals.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.RAIN;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.SNOW;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherFiltersMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$skipPrecipitation(CallbackInfo callback, @Local(argsOnly = true) WeatherRenderState state) {
        if (!NoRender.hides(RAIN) || !NoRender.hides(SNOW)) return;
        state.reset();
        callback.cancel();
    }
}

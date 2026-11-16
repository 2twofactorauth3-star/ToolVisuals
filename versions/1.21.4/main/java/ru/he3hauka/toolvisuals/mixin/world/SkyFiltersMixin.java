package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

@Mixin(SkyRenderer.class)
public abstract class SkyFiltersMixin {
    @Inject(method="renderSun",at=@At("HEAD"),cancellable=true) private void toolvisuals$sun(CallbackInfo callback){if(NoRender.hides(RenderOption.SUN))callback.cancel();}
    @Inject(method="renderMoon",at=@At("HEAD"),cancellable=true) private void toolvisuals$moon(CallbackInfo callback){if(NoRender.hides(RenderOption.MOON))callback.cancel();}
    @Inject(method="renderSunriseAndSunset",at=@At("HEAD"),cancellable=true) private void toolvisuals$sunrise(CallbackInfo callback){if(NoRender.hides(RenderOption.SUNRISE))callback.cancel();}
    @Inject(method="renderStars",at=@At("HEAD"),cancellable=true) private void toolvisuals$stars(CallbackInfo callback){if(NoRender.hides(RenderOption.STARS))callback.cancel();}
}

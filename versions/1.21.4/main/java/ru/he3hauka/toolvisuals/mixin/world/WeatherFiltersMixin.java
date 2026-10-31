package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.WORLD_CUSTOMIZER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.CLEAR_WEATHER;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherFiltersMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipPrecipitation(CallbackInfo callback){if(toolvisuals$clear()||NoRender.hides(RAIN)&&NoRender.hides(SNOW))callback.cancel();}
    private static boolean toolvisuals$clear(){return ToolVisualsClient.CONFIG.active(WORLD_CUSTOMIZER)&&ToolVisualsClient.CONFIG.flag(CLEAR_WEATHER);}
    @Inject(method="getPrecipitationAt",at=@At("RETURN"),cancellable=true) private void toolvisuals$weather(CallbackInfoReturnable<Biome.Precipitation> callback){var weather=callback.getReturnValue();if(toolvisuals$clear()||weather==Biome.Precipitation.RAIN&&NoRender.hides(RAIN)||weather==Biome.Precipitation.SNOW&&NoRender.hides(SNOW))callback.setReturnValue(Biome.Precipitation.NONE);}
    @Inject(method="tickRainParticles",at=@At("HEAD"),cancellable=true) private void toolvisuals$rainParticles(CallbackInfo callback){if(toolvisuals$clear()||NoRender.hides(RAIN))callback.cancel();}
}

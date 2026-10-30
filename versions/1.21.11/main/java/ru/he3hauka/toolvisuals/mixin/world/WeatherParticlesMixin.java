package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.WORLD_CUSTOMIZER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.CLEAR_WEATHER;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherParticlesMixin {
    @Inject(method="tickRainParticles",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$clear(CallbackInfo callback){if(ToolVisualsClient.CONFIG.active(WORLD_CUSTOMIZER)&&ToolVisualsClient.CONFIG.flag(CLEAR_WEATHER))callback.cancel();}
}

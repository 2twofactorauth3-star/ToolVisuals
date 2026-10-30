package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.optimization.lighting.FullBrightState;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.FULL_BRIGHT;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.BRIGHTNESS;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapExtractorMixin {
    @Shadow private boolean needsUpdate;
    @Shadow @Final private Minecraft minecraft;
    @Unique private final FullBrightState toolvisuals$brightnessState = new FullBrightState();

    @Inject(method = "extract", at = @At("HEAD"))
    private void toolvisuals$invalidateBrightness(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (toolvisuals$brightnessState.update(minecraft.level, ToolVisualsClient.CONFIG.active(FULL_BRIGHT),
                ToolVisualsClient.CONFIG.number(BRIGHTNESS))) needsUpdate = true;
    }

    @Inject(method = "extract", at = @At("TAIL"))
    private void toolvisuals$brightness(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (state.needsUpdate && ToolVisualsClient.CONFIG.active(FULL_BRIGHT)) {
            state.nightVisionEffectIntensity = ToolVisualsClient.CONFIG.number(BRIGHTNESS);
            state.nightVisionColor = LightmapRenderStateExtractor.WHITE;
        }
    }
}

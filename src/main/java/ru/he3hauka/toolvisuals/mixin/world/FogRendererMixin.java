package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.NoFluid;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.FOG;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void toolvisuals$hideFog(CallbackInfoReturnable<FogData> cir) {
        var config = ToolVisualsClient.CONFIG;
        float distance = config.number(VisualSetting.FOG_DISTANCE);
        if (config.active(VisualModule.WORLD_CUSTOMIZER) && distance > 0) {
            FogData fog = cir.getReturnValue();
             
            if (Minecraft.getInstance().gameRenderer.mainCamera().getFluidInCamera()
                    == FogType.NONE) {
                fog.environmentalStart = distance * .4f; fog.environmentalEnd = distance;
                fog.renderDistanceStart = distance * .4f; fog.renderDistanceEnd = distance;
            }
        }
        if (NoRender.hides(FOG) || config.flag(VisualSetting.NO_FLUID_FOG)
                && NoFluid.clears(Minecraft.getInstance().gameRenderer.mainCamera().getFluidInCamera())) {
            FogData fog = cir.getReturnValue();
             
            fog.environmentalStart = 1_000_000;
            fog.environmentalEnd = 2_000_000;
            fog.renderDistanceStart = 1_000_000;
            fog.renderDistanceEnd = 2_000_000;
            fog.skyEnd = 2_000_000;
            fog.cloudEnd = 2_000_000;
        }
    }
}


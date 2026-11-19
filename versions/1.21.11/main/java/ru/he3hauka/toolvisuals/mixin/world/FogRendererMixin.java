package ru.he3hauka.toolvisuals.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.NoFluid;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Inject(method="setupFog",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/systems/RenderSystem;getDevice()Lcom/mojang/blaze3d/systems/GpuDevice;"))
    private void toolvisuals$fog(CallbackInfoReturnable<Vector4f> callback,@Local FogData fog) {
        var config=ToolVisualsClient.CONFIG;
        var fluid=Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera();
        if(NoRender.hides(RenderOption.FOG)||config.flag(VisualSetting.NO_FLUID_FOG)&&NoFluid.clears(fluid)){
            fog.environmentalStart=fog.renderDistanceStart=1_000_000;
            fog.environmentalEnd=fog.renderDistanceEnd=fog.skyEnd=fog.cloudEnd=2_000_000;
        }else if(fluid==FogType.NONE&&config.active(VisualModule.WORLD_CUSTOMIZER)&&config.number(VisualSetting.FOG_DISTANCE)>0){
            float distance=config.number(VisualSetting.FOG_DISTANCE);
            fog.environmentalStart=fog.renderDistanceStart=distance*.4f;
            fog.environmentalEnd=fog.renderDistanceEnd=distance;
        }
    }
}

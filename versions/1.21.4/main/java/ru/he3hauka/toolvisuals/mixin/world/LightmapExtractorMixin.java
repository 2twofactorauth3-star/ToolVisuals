package ru.he3hauka.toolvisuals.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.optimization.lighting.FullBrightState;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.FULL_BRIGHT;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.BRIGHTNESS;

@Mixin(LightTexture.class)
public abstract class LightmapExtractorMixin {
    @Shadow private boolean updateLightTexture;
    @Unique private final FullBrightState toolvisuals$brightness=new FullBrightState();
    @Inject(method="updateLightTexture",at=@At("HEAD")) private void toolvisuals$invalidate(CallbackInfo callback){var client=Minecraft.getInstance();if(toolvisuals$brightness.update(client.level,ToolVisualsClient.CONFIG.active(FULL_BRIGHT),ToolVisualsClient.CONFIG.number(BRIGHTNESS)))updateLightTexture=true;}
    @WrapOperation(method="updateLightTexture",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/shaders/AbstractUniform;set(F)V"))
    private void toolvisuals$nightVision(com.mojang.blaze3d.shaders.AbstractUniform uniform,float value,Operation<Void> original){if(uniform instanceof Uniform named&&named.getName().equals("NightVisionFactor")&&ToolVisualsClient.CONFIG.active(FULL_BRIGHT))value=ToolVisualsClient.CONFIG.number(BRIGHTNESS);original.call(uniform,value);}
}

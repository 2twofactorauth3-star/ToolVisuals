package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.render.filter.NoFluid;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.WORLD_CUSTOMIZER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Inject(method="setupFog",at=@At("RETURN"),cancellable=true)
    private static void toolvisuals$fog(CallbackInfoReturnable<FogParameters> callback){var fog=callback.getReturnValue();var config=ToolVisualsClient.CONFIG;var fluid=Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera();float start=fog.start(),end=fog.end();if(NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.FOG)||config.flag(NO_FLUID_FOG)&&NoFluid.clears(fluid)){start=1_000_000;end=2_000_000;}else if(config.active(WORLD_CUSTOMIZER)&&config.value(FOG_DISTANCE)>0&&fluid==net.minecraft.world.level.material.FogType.NONE){end=config.number(FOG_DISTANCE);start=end*.4f;}if(start!=fog.start()||end!=fog.end())callback.setReturnValue(new FogParameters(start,end,fog.shape(),fog.red(),fog.green(),fog.blue(),fog.alpha()));}
}

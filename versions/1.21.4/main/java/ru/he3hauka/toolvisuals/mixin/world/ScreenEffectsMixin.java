package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.NoFluid;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectsMixin {
    private static boolean toolvisuals$clearFluid(){return ToolVisualsClient.CONFIG.flag(VisualSetting.NO_FLUID_OVERLAY)&&NoFluid.clears(Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera());}
    @Inject(method="renderWater",at=@At("HEAD"),cancellable=true) private static void toolvisuals$water(CallbackInfo callback){if(NoRender.hides(WATER_OVERLAY)||toolvisuals$clearFluid())callback.cancel();}
    @Inject(method="renderFire",at=@At("HEAD"),cancellable=true) private static void toolvisuals$fire(CallbackInfo callback){if(NoRender.hides(FIRE_OVERLAY)||toolvisuals$clearFluid())callback.cancel();}
}

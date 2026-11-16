package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.WorldBorderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

@Mixin(WorldBorderRenderer.class)
public abstract class BorderFilterMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true) private void toolvisuals$border(CallbackInfo callback){if(NoRender.hides(RenderOption.WORLD_BORDER))callback.cancel();}
}

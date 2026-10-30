package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class TotemFilterMixin {
    @Inject(method="renderItemActivationAnimation",at=@At("HEAD"),cancellable=true) private void toolvisuals$totem(CallbackInfo callback){if(ru.he3hauka.toolvisuals.client.render.filter.NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.TOTEM_ANIMATION))callback.cancel();}
}

package ru.he3hauka.toolvisuals.mixin.hud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.components.SubtitleOverlay.class)
public abstract class SubtitleOverlayMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$hide(CallbackInfo callback){if(ru.he3hauka.toolvisuals.client.render.filter.NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.SUBTITLES))callback.cancel();}
}

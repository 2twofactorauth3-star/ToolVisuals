package ru.he3hauka.toolvisuals.mixin.hud;

import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

@Mixin(SubtitleOverlay.class)
public abstract class SubtitleOverlayMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$hide(CallbackInfo callback){if(NoRender.hides(RenderOption.SUBTITLES))callback.cancel();}
}

package ru.he3hauka.toolvisuals.mixin.hud;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugOverlayMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$hide(CallbackInfo callback){if(NoRender.hides(RenderOption.DEBUG_OVERLAY))callback.cancel();}
}

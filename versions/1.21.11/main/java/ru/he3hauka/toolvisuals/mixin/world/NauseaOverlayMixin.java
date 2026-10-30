package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

@Mixin(Gui.class)
public abstract class NauseaOverlayMixin {
    @Inject(method="renderConfusionOverlay",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$nausea(CallbackInfo callback){if(NoRender.hides(RenderOption.NAUSEA))callback.cancel();}
}

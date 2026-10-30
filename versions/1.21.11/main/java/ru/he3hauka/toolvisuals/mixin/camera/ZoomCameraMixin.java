package ru.he3hauka.toolvisuals.mixin.camera;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.zoom.VisualZoom;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(GameRenderer.class)
public abstract class ZoomCameraMixin {
    @Inject(method="getFov",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$zoom(CallbackInfoReturnable<Float> callback){if(!ReplayScope.active())callback.setReturnValue(VisualZoom.fov(callback.getReturnValue()));}
}

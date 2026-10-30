package ru.he3hauka.toolvisuals.mixin.camera;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.zoom.VisualZoom;

@Mixin(Camera.class)
public abstract class ZoomCameraMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void toolvisuals$zoom(float partial, CallbackInfoReturnable<Float> cir) {
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;
        cir.setReturnValue(VisualZoom.fov(cir.getReturnValue()));
    }
}

package ru.he3hauka.toolvisuals.mixin.camera;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import ru.he3hauka.toolvisuals.client.camera.FreeLook;

@Mixin(Camera.class)
public abstract class FreeLookCameraMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void toolvisuals$updateFreeLook(DeltaTracker delta, CallbackInfo ci) { FreeLook.update(Minecraft.getInstance()); }
    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void toolvisuals$freeLookRotation(Args args) {
        if (FreeLook.active()) { args.set(0, FreeLook.yaw()); args.set(1, FreeLook.pitch()); }
    }
}

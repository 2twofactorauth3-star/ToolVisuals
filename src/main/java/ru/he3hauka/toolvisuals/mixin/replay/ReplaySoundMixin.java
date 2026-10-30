package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(SoundManager.class)
public abstract class ReplaySoundMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$noReplaySound(CallbackInfoReturnable<SoundEngine.PlayResult> callback) {
        if (ReplayScope.active()) callback.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
    }
    @Inject(method = {"playDelayed", "queueTickingSound", "stop", "updateSource"}, at = @At("HEAD"), cancellable = true)
    private void toolvisuals$keepLiveSound(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

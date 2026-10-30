package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(Options.class)
public abstract class ReplayOptionsMixin {
    @Inject(method = "setServerRenderDistance", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$keepLiveDistance(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

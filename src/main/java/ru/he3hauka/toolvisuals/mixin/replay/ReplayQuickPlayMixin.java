package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.quickplay.QuickPlayLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(QuickPlayLog.class)
public abstract class ReplayQuickPlayMixin {
    @Inject(method = "log", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$keepLiveQuickPlay(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

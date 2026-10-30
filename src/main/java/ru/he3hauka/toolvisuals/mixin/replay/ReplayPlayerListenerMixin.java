package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(ClientPacketListener.class)
public abstract class ReplayPlayerListenerMixin {
    @Inject(method = "prepareKeyPair", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$noReplayAuthentication(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

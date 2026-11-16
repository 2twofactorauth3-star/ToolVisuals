package ru.he3hauka.toolvisuals.mixin.replay;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(ClientPacketListener.class)
public abstract class ReplayPlayerListenerMixin {
    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void toolvisuals$rememberLogin(CallbackInfo callback, @Local(argsOnly = true) ClientboundLoginPacket packet) {
        if (!ReplayScope.active()) ReplayCapture.login((ClientPacketListener)(Object)this, packet);
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void toolvisuals$rememberRespawn(CallbackInfo callback, @Local(argsOnly = true) ClientboundRespawnPacket packet) {
        if (!ReplayScope.active()) ReplayCapture.respawn((ClientPacketListener)(Object)this, packet);
    }

    @Inject(method = "prepareKeyPair", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$noReplayAuthentication(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

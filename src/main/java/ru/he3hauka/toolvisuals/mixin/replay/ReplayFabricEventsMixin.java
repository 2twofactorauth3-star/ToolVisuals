package ru.he3hauka.toolvisuals.mixin.replay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(targets = {"net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon", "net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon"}, remap = false)
public abstract class ReplayFabricEventsMixin {
    @Inject(method = {"invokeInitEvent", "onServerReady", "invokeDisconnectEvent"}, at = @At("HEAD"), cancellable = true)
    private void toolvisuals$localReplayEvents(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

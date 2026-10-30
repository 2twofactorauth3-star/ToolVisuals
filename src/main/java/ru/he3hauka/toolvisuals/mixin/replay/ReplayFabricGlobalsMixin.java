package ru.he3hauka.toolvisuals.mixin.replay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(targets = "net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl", remap = false)
public abstract class ReplayFabricGlobalsMixin {
    @Inject(method = {"setClientPlayAddon", "setClientConfigurationAddon"}, at = @At("HEAD"), cancellable = true)
    private static void toolvisuals$keepLiveNetworking(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

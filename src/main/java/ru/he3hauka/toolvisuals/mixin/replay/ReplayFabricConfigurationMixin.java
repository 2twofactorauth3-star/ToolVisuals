package ru.he3hauka.toolvisuals.mixin.replay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(targets = "net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon", remap = false)
public abstract class ReplayFabricConfigurationMixin {
    @Inject(method = {"handleComplete", "invokeStartEvent"}, at = @At("HEAD"), cancellable = true)
    private void toolvisuals$localReplayConfiguration(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

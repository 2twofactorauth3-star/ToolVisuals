package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(PlayerSocialManager.class)
public abstract class ReplaySocialMixin {
    @Inject(method = {"addPlayer", "removePlayer", "startOnlineMode", "stopOnlineMode"}, at = @At("HEAD"), cancellable = true)
    private void toolvisuals$keepLiveSocialState(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

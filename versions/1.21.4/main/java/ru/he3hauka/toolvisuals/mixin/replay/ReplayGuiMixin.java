package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(Minecraft.class)
public abstract class ReplayGuiMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$keepReplayScreen(CallbackInfo callback) { if (ReplayScope.active()) callback.cancel(); }
}

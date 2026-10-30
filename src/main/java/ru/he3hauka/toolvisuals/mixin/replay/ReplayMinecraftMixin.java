package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

@Mixin(Minecraft.class)
public abstract class ReplayMinecraftMixin {
    @Inject(method = "setLevel", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$replayLevel(ClientLevel level, CallbackInfo callback) {
        if (ReplayScope.active()) { ReplayScope.scene().level(level); callback.cancel(); }
    }
    @Inject(method="clearClientLevel",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$clearReplayLevel(CallbackInfo callback){
        if(ReplayScope.active()){ReplayScope.scene().clearLevel();callback.cancel();}
    }
    @Inject(method = "getSingleplayerServer", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$replayServer(CallbackInfoReturnable<IntegratedServer> callback) {
        if (ReplayScope.active()) callback.setReturnValue(null);
    }
    @Inject(method = "hasSingleplayerServer", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$replayLocal(CallbackInfoReturnable<Boolean> callback) {
        if (ReplayScope.active()) callback.setReturnValue(false);
    }
}

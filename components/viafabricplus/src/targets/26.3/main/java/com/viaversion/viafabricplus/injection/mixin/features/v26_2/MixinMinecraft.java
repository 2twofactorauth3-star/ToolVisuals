package com.viaversion.viafabricplus.injection.mixin.features.v26_2;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    @Nullable
    public ClientLevel level;

    @WrapWithCondition(method = {"continueAttack", "startAttack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private boolean dontSendPunch(ClientPacketListener instance, Packet<?> packet) {

        return ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3);
    }

    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z"))
    private void addBreakingParticles(boolean down, CallbackInfo ci, @Local(name = "pos") BlockPos pos, @Local(name = "direction") Direction direction) {

        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            this.level.addBreakingBlockEffects(pos, direction, false);
        }
    }

    @WrapWithCondition(method = "pauseGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"))
    private boolean dontStopDestroyingOnPause(MultiPlayerGameMode instance) {
        return ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3);
    }

}

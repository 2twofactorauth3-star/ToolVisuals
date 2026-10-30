package com.viaversion.viafabricplus.injection.mixin.features.v26_2.network;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener extends ClientCommonPacketListenerImpl {

    protected MixinClientPacketListener(final Minecraft minecraft, final Connection connection, final CommonListenerCookie cookie) {
        super(minecraft, connection, cookie);
    }

    @Inject(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z", shift = At.Shift.AFTER, ordinal = 0))
    private void confirmRemovedVehicleTeleport(ClientboundTeleportEntityPacket packet, CallbackInfo ci) {

        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            final LocalPlayer player = this.minecraft.player;
            this.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false, false));
        }
    }

    @WrapWithCondition(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"))
    private boolean dontStopDestroyingOnTeleport(MultiPlayerGameMode instance) {
        return ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3);
    }

}

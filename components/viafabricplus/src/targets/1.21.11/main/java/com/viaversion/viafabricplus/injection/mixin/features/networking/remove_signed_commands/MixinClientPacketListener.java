package com.viaversion.viafabricplus.injection.mixin.features.networking.remove_signed_commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.time.Instant;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener extends ClientCommonPacketListenerImpl {

    @Shadow
    private LastSeenMessagesTracker lastSeenMessages;

    protected MixinClientPacketListener(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Redirect(method = "sendCommand", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean alwaysSignCommands(List<?> instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_20_3) && instance.isEmpty();
    }

    @Redirect(method = "sendUnattendedCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void alwaysSignCommands(ClientPacketListener instance, Packet<?> packet, @Local(argsOnly = true) String command) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_3)) {
            this.send(new ServerboundChatCommandSignedPacket(command, Instant.now(), 0L, ArgumentSignatures.EMPTY, this.lastSeenMessages.generateAndApplyUpdate().update()));
        } else {
            instance.send(packet);
        }
    }

}

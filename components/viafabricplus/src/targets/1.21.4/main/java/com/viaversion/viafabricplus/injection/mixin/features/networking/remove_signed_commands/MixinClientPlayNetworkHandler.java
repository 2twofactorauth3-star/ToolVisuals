package com.viaversion.viafabricplus.injection.mixin.features.networking.remove_signed_commands;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.time.Instant;
import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {

    @Shadow
    private LastSeenMessagesCollector lastSeenMessagesCollector;

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Redirect(method = "sendChatCommand", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean alwaysSignCommands(List<?> instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_3)) {
            return false;
        } else {
            return instance.isEmpty();
        }
    }

    @Redirect(method = "sendCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void alwaysSignCommands(ClientPlayNetworkHandler instance, Packet<?> packet, @Local(argsOnly = true) String command) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_3)) {
            this.sendPacket(new ChatCommandSignedC2SPacket(command, Instant.now(), 0L, ArgumentSignatureDataMap.EMPTY, this.lastSeenMessagesCollector.collect().update()));
        } else {
            instance.sendPacket(packet);
        }
    }

}

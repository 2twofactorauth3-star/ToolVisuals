package com.viaversion.viafabricplus.injection.mixin.features.networking.packet_handling;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class MixinClientCommonNetworkHandler {

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    protected abstract void send(Packet<? extends ServerPacketListener> packet, BooleanSupplier sendCondition, Duration expiry);

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow
    @Final
    protected ClientConnection connection;

    @Shadow
    @Nullable
    private static URL getParsedResourcePackUrl(String url) {
        return null;
    }

    @Inject(method = "savePacketErrorReport", at = @At("HEAD"), cancellable = true)
    private void dontCreatePacketErrorCrashReports(CallbackInfoReturnable<Optional<Path>> cir) {
        if (DebugSettings.INSTANCE.dontCreatePacketErrorCrashReports.isEnabled()) {
            cir.setReturnValue(Optional.empty());
        }
    }

    @WrapWithCondition(method = "onPacketException", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;disconnect(Lnet/minecraft/network/DisconnectionInfo;)V"))
    private boolean dontDisconnectOnPacketException(ClientConnection instance, DisconnectionInfo disconnectionInfo) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_20_3);
    }

    @Inject(method = "onResourcePackSend", at = @At("HEAD"), cancellable = true)
    private void validateUrlInNetworkThread(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_2)) {
            if (getParsedResourcePackUrl(packet.url()) == null) {
                this.connection.send(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.INVALID_URL));
                ci.cancel();
            }
        }
    }

    @Redirect(method = "onKeepAlive", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientCommonNetworkHandler;send(Lnet/minecraft/network/packet/Packet;Ljava/util/function/BooleanSupplier;Ljava/time/Duration;)V"))
    private void forceSendKeepAlive(ClientCommonNetworkHandler instance, Packet<? extends ServerPacketListener> packet, BooleanSupplier sendCondition, Duration expiry) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_3)) {
            sendPacket(packet);
        } else {
            send(packet, sendCondition, expiry);
        }
    }

    @Inject(method = "onPing", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void addMissingConditions(CommonPingS2CPacket packet, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_4)) {
            final short inventoryId = (short) ((packet.getParameter() >> 16) & 0xFF);
            if (inventoryId != 0 && inventoryId != client.player.currentScreenHandler.syncId) {
                ci.cancel();
            }
        }
    }

}

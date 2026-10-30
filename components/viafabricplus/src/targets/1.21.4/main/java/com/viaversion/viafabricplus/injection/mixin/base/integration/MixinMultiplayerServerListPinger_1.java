package com.viaversion.viafabricplus.injection.mixin.base.integration;

import net.minecraft.SharedConstants;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;

import com.viaversion.viafabricplus.injection.access.base.IClientConnection;
import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.network.MultiplayerServerListPinger$1")
public abstract class MixinMultiplayerServerListPinger_1 implements ClientQueryPacketListener {

    @Final
    @Shadow
    ServerInfo field_3776;

    @Shadow
    @Final
    ClientConnection field_3774;

    @Inject(method = "onResponse(Lnet/minecraft/network/packet/s2c/query/QueryResponseS2CPacket;)V", at = @At("HEAD"))
    private void trackTranslatingState(QueryResponseS2CPacket packet, CallbackInfo ci) {

        if (field_3774 instanceof IClientConnection mixinClientConnection) {
            ((IServerInfo) field_3776).viaFabricPlus$setTranslatingVersion(mixinClientConnection.viaFabricPlus$getTargetVersion());
        }
    }

    @Inject(method = "onResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;)V", shift = At.Shift.AFTER))
    private void fixVersionComparison(CallbackInfo ci) {
        final ProtocolVersion version = ((IClientConnection) this.field_3774).viaFabricPlus$getTargetVersion();

        if (version != null && version.getVersion() == this.field_3776.protocolVersion) {
            this.field_3776.protocolVersion = SharedConstants.getProtocolVersion();
        }
    }

}

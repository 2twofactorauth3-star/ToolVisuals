package com.viaversion.viafabricplus.injection.mixin.features.networking.limitation;

import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class MixinClientLoginNetworkHandler {

    @Redirect(method = "onCompression", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;setCompressionThreshold(IZ)V"))
    private void pre1_17_1CompressionBehaviour(ClientConnection instance, int compressionThreshold, boolean rejectsBadPackets) {
        instance.setCompressionThreshold(compressionThreshold, ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_17));
    }

}

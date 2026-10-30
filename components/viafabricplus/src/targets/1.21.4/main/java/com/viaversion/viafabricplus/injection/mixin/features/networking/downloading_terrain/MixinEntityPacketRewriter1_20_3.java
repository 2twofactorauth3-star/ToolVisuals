package com.viaversion.viafabricplus.injection.mixin.features.networking.downloading_terrain;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.EntityPacketRewriter1_20_3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPacketRewriter1_20_3.class, remap = false)
public abstract class MixinEntityPacketRewriter1_20_3 {

    @Inject(method = "sendChunksSentGameEvent", at = @At("HEAD"), cancellable = true)
    private void cancelGameStatePacket(PacketWrapper wrapper, CallbackInfo ci) {
        ci.cancel();
    }

}

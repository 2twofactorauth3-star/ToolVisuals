package com.viaversion.viafabricplus.injection.mixin.features.networking.config_state;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.ConfigurationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ConfigurationState.class, remap = false)
public abstract class MixinConfigurationState {

    @Inject(method = "addPacketToQueue", at = @At("HEAD"), cancellable = true)
    private void cancelQueuedPackets(PacketWrapper wrapper, boolean clientbound, CallbackInfo ci) {

        if (clientbound && wrapper.getPacketType() == null) {
            final int unmappedId = wrapper.getId();
            if (unmappedId == ClientboundPackets1_19_4.CONTAINER_CLOSE.getId() || unmappedId == ClientboundPackets1_19_4.CHUNKS_BIOMES.getId()) {
                ci.cancel();
            }
        }
    }

}

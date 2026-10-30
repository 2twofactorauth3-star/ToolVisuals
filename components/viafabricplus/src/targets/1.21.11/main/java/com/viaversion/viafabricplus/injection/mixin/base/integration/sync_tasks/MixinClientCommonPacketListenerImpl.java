package com.viaversion.viafabricplus.injection.mixin.base.integration.sync_tasks;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

import com.viaversion.viafabricplus.base.sync_tasks.DataCustomPayload;
import com.viaversion.viafabricplus.base.sync_tasks.SyncTasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientCommonPacketListenerImpl.class, priority = 1  )
public abstract class MixinClientCommonPacketListenerImpl {

    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V", at = @At("HEAD"), cancellable = true)
    private void handleSyncTask(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof DataCustomPayload(FriendlyByteBuf buf)) {
            SyncTasks.handleSyncTask(buf);
            ci.cancel();
        }
    }

}

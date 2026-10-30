package com.viaversion.viafabricplus.injection.mixin.base.integration.sync_tasks;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

import com.viaversion.viafabricplus.base.sync_tasks.DataCustomPayload;
import com.viaversion.viafabricplus.base.sync_tasks.SyncTasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientCommonNetworkHandler.class, priority = 1  )
public abstract class MixinClientCommonNetworkHandler {

    @Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V", at = @At("HEAD"), cancellable = true)
    private void handleSyncTask(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof DataCustomPayload(net.minecraft.network.PacketByteBuf buf)) {
            SyncTasks.handleSyncTask(buf);
            ci.cancel();
        }
    }

}

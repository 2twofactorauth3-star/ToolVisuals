package ru.he3hauka.toolvisuals.mixin.replay;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture;

@Mixin(PacketUtils.class)
public abstract class ReplayDispatchMixin {
    @Inject(method = "ensureRunningOnSameThread", at = @At("RETURN"))
    private static void toolvisuals$recordDispatched(CallbackInfo callback, @Local(argsOnly = true) Packet<?> packet,
                                                   @Local(argsOnly = true) PacketListener listener) {
        if (listener instanceof ClientPacketListener client) ReplayCapture.dispatched(client, packet);
    }
}

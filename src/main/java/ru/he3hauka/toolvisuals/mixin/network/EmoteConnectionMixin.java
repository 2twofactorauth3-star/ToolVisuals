package ru.he3hauka.toolvisuals.mixin.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.animation.network.EmotePacketGuard;

@Mixin(Connection.class)
public abstract class EmoteConnectionMixin {
    @Unique private final EmotePacketGuard toolvisuals$traffic = new EmotePacketGuard(System.nanoTime());

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$limitTraffic(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        if (toolvisuals$traffic.allow(packet, System.nanoTime())) return;
        callback.cancel();
        ((Connection) (Object) this).disconnect(Component.literal("ToolVisuals animation packet rate exceeded"));
    }
}

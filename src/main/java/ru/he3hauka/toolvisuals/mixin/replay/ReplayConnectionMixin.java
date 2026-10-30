package ru.he3hauka.toolvisuals.mixin.replay;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture;

@Mixin(Connection.class)
public abstract class ReplayConnectionMixin {
    @Unique private ProtocolInfo<?> toolvisuals$inbound;
    @Inject(method="send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$keepReplayTrafficLocal(CallbackInfo callback){
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active()
                &&!((Object)this instanceof ru.he3hauka.toolvisuals.client.replay.capture.VirtualReplayConnection))callback.cancel();
    }

    @Inject(method = "setupInboundProtocol", at = @At("TAIL"))
    private void toolvisuals$protocol(ProtocolInfo<?> protocol, PacketListener listener, CallbackInfo callback) {
        toolvisuals$inbound = protocol;
        ReplayCapture.protocol((Connection)(Object)this, protocol);
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void toolvisuals$recordMemory(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        var connection = (Connection)(Object)this;
        if (ReplayCapture.recording() && connection.isMemoryConnection()) ReplayCapture.memory(connection, toolvisuals$inbound, packet);
    }
}

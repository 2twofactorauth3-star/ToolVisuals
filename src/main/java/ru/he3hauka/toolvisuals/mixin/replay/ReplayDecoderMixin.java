package ru.he3hauka.toolvisuals.mixin.replay;

import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture;

@Mixin(PacketDecoder.class)
public abstract class ReplayDecoderMixin {
    @Shadow @Final private ProtocolInfo<?> protocolInfo;
    @Unique private int toolvisuals$start, toolvisuals$output;

    @Inject(method = "decode", at = @At("HEAD"))
    private void toolvisuals$beforeDecode(ChannelHandlerContext context, ByteBuf buffer, List<Object> output, CallbackInfo callback) {
        toolvisuals$start = buffer.readerIndex(); toolvisuals$output = output.size();
    }

    @Inject(method = "decode", at = @At("RETURN"))
    private void toolvisuals$afterDecode(ChannelHandlerContext context, ByteBuf buffer, List<Object> output, CallbackInfo callback) {
        if (!ReplayCapture.recording() || output.size() != toolvisuals$output + 1 || !(output.getLast() instanceof Packet<?> packet)) return;
        var connection = context.pipeline().get(Connection.class);
        if (connection != null) ReplayCapture.raw(connection, protocolInfo, packet, buffer, toolvisuals$start, buffer.readerIndex());
    }
}

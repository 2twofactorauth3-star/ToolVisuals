package ru.he3hauka.toolvisuals.mixin.optimization.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.zip.Deflater;
import net.minecraft.network.CompressionEncoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.network.NetworkOptimizations;
import ru.he3hauka.toolvisuals.client.optimization.network.PacketCompression;

@Mixin(CompressionEncoder.class)
public abstract class CompressionEncoderMixin {
    @Shadow @Final private Deflater deflater;
    @Shadow private int threshold;
    @Inject(method="encode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Lio/netty/buffer/ByteBuf;)V",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$encode(ChannelHandlerContext context,ByteBuf input,ByteBuf output,CallbackInfo callback){if(NetworkOptimizations.enabled()){PacketCompression.encode(input,output,threshold,deflater);callback.cancel();}}
}

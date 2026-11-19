package ru.he3hauka.toolvisuals.mixin.optimization.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Varint21FrameDecoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.network.NetworkOptimizations;
import ru.he3hauka.toolvisuals.client.optimization.network.PacketFrames;

@Mixin(Varint21FrameDecoder.class)
public abstract class FrameDecoderMixin {
    @Shadow @Final private BandwidthDebugMonitor monitor;
    @Inject(method="decode",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$decode(ChannelHandlerContext context,ByteBuf input,List<Object> output,CallbackInfo callback){
        if(!NetworkOptimizations.enabled())return;
        callback.cancel();
        if(!context.channel().isActive()){input.clear();return;}
        int bytes=PacketFrames.read(input,output);
        if(monitor!=null&&bytes>0)monitor.onReceive(bytes);
    }
}

package ru.he3hauka.toolvisuals.client.optimization.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import java.util.function.BooleanSupplier;

@ChannelHandler.Sharable
public final class PacketFrameEncoder extends MessageToMessageEncoder<ByteBuf> {
    public static final PacketFrameEncoder INSTANCE=new PacketFrameEncoder(NetworkOptimizations::enabled);
    private final BooleanSupplier enabled;
    PacketFrameEncoder(BooleanSupplier enabled){this.enabled=enabled;}
    @Override public void write(ChannelHandlerContext context,Object message,ChannelPromise promise) throws Exception {
        boolean optimized=enabled.getAsBoolean();
        NetworkCounter counter=optimized?context.channel().attr(NetworkMeasurement.COUNTER).get():null;
        if(counter!=null&&message instanceof ByteBuf input&&PacketBytes.size(input.readableBytes())<=3){
            int length=input.readableBytes();
            promise=promise.unvoid();
            promise.addListener(result->{if(result.isSuccess())counter.sent(length);});
        }
        super.write(context,message,promise);
    }
    @Override protected void encode(ChannelHandlerContext context,ByteBuf message,List<Object> output){
        int length=message.readableBytes(),headerLength=PacketBytes.size(length);
        if(headerLength>3)throw new EncoderException("Packet frame exceeds 21 bits");
        if(!enabled.getAsBoolean()){
            ByteBuf frame=context.alloc().buffer(headerLength+length);
            try{PacketBytes.write(frame,length);frame.writeBytes(message,message.readerIndex(),length);output.add(frame);}catch(Throwable error){frame.release();throw error;}
            return;
        }
        ByteBuf header=context.alloc().buffer(headerLength,headerLength);
        try {PacketBytes.write(header,length);output.add(header);}catch(Throwable error){header.release();throw error;}
        output.add(message.retain());
        var counter=context.channel().attr(NetworkMeasurement.COUNTER).get();
        if(counter!=null)counter.avoidedCopy(length);
    }
}

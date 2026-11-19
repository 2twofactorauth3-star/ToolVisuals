package ru.he3hauka.toolvisuals.client.optimization.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@ChannelHandler.Sharable
public final class PacketFrameEncoder extends MessageToMessageEncoder<ByteBuf> {
    public static final PacketFrameEncoder INSTANCE=new PacketFrameEncoder();
    private PacketFrameEncoder() {}
    @Override protected void encode(ChannelHandlerContext context,ByteBuf message,List<Object> output){
        int length=message.readableBytes(),headerLength=PacketBytes.size(length);
        if(headerLength>3)throw new EncoderException("Packet frame exceeds 21 bits");
        ByteBuf header=context.alloc().buffer(headerLength,headerLength);
        try {PacketBytes.write(header,length);output.add(header);}catch(Throwable error){header.release();throw error;}
        output.add(message.retain());
    }
}

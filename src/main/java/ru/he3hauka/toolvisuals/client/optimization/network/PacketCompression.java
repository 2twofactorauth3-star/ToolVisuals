package ru.he3hauka.toolvisuals.client.optimization.network;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

public final class PacketCompression {
    private PacketCompression() {}
    public static void encode(ByteBuf input,ByteBuf output,int threshold,Deflater deflater){
        int length=input.readableBytes();
        if(length>8*1024*1024)throw new EncoderException("Uncompressed packet exceeds 8 MiB");
        if(length<threshold){PacketBytes.write(output,0);output.writeBytes(input);return;}
        PacketBytes.write(output,length);
        try {
            ByteBuffer source=input.nioBuffer();
            deflater.setInput(source);deflater.finish();
            while(!deflater.finished()){
                output.ensureWritable(8192);
                ByteBuffer destination=output.internalNioBuffer(output.writerIndex(),output.writableBytes());
                int count=deflater.deflate(destination);
                if(count==0&&!deflater.finished())throw new EncoderException("Compression made no progress");
                output.writerIndex(output.writerIndex()+count);
            }
            input.skipBytes(length);
        } finally {deflater.reset();}
    }
}

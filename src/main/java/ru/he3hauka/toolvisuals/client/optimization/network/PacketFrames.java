package ru.he3hauka.toolvisuals.client.optimization.network;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import java.util.List;

public final class PacketFrames {
    private PacketFrames() {}
    public static int read(ByteBuf input,List<Object> output){
        if(!input.isReadable())return 0;
        if(input.getByte(input.readerIndex())==0){
            int first=input.forEachByte(ByteProcessor.FIND_NON_NUL);
            if(first<0){input.skipBytes(input.readableBytes());return 0;}
            input.readerIndex(first);
        }
        int start=input.readerIndex(),length=PacketBytes.frameLength(input);
        if(length<0)return 0;
        if(input.readableBytes()<length){input.readerIndex(start);return 0;}
        if(length==0)return 0;
        int bytes=length+input.readerIndex()-start;
        output.add(input.readRetainedSlice(length));return bytes;
    }
}

package ru.he3hauka.toolvisuals.client.optimization.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;
import java.nio.ByteOrder;

public final class PacketBytes {
    private static final int[] LENGTHS=new int[33];
    static {for(int zeros=0;zeros<32;zeros++)LENGTHS[zeros]=(38-zeros)/7;LENGTHS[32]=1;}
    private PacketBytes() {}
    public static int size(int value){return LENGTHS[Integer.numberOfLeadingZeros(value)];}
    public static ByteBuf write(ByteBuf buffer,int value){
        if(buffer.order()==ByteOrder.LITTLE_ENDIAN){
            while((value&~127)!=0){buffer.writeByte(value&127|128);value>>>=7;}
            buffer.writeByte(value);return buffer;
        }
        int length=size(value);
        switch(length){
            case 1->buffer.writeByte(value);
            case 2->buffer.writeShort((value&127|128)<<8|value>>>7);
            case 3->buffer.writeMedium((value&127|128)<<16|(value>>>7&127|128)<<8|value>>>14);
            case 4->buffer.writeInt((value&127|128)<<24|(value>>>7&127|128)<<16|(value>>>14&127|128)<<8|value>>>21);
            default->{buffer.writeInt((value&127|128)<<24|(value>>>7&127|128)<<16|(value>>>14&127|128)<<8|(value>>>21&127|128));buffer.writeByte(value>>>28);}
        }
        return buffer;
    }
    public static void utf8(ByteBuf buffer,CharSequence text,int maximum){
        if(text.length()>maximum)throw new EncoderException("String exceeds character limit "+maximum);
        int bytes=ByteBufUtil.utf8Bytes(text);
        if(bytes>ByteBufUtil.utf8MaxBytes(maximum))throw new EncoderException("String exceeds UTF-8 byte limit");
        write(buffer,bytes);buffer.writeCharSequence(text,StandardCharsets.UTF_8);
    }
    public static int frameLength(ByteBuf buffer){
        int available=buffer.readableBytes(),index=buffer.readerIndex();
        if(available==0)return -1;
        if(available>=4){
            int word=buffer.getIntLE(index),stop=~word&0x808080;
            if(stop==0)throw new DecoderException("Packet length VarInt exceeds 21 bits");
            int length=(Integer.numberOfTrailingZeros(stop)+1)>>3;
            int bytes=word&(stop^(stop-1));
            bytes=bytes&0x007f007f|(bytes&0x00007f00)>>>1;
            bytes=bytes&0x00003fff|(bytes&0x3fff0000)>>>2;
            buffer.skipBytes(length);return bytes;
        }
        int result=0;
        for(int offset=0;offset<Math.min(3,available);offset++){
            int value=buffer.getUnsignedByte(index+offset);result|=(value&127)<<(offset*7);
            if((value&128)==0){buffer.skipBytes(offset+1);return result;}
        }
        if(available>=3)throw new DecoderException("Packet length VarInt exceeds 21 bits");
        return -1;
    }
}

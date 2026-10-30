package ru.he3hauka.toolvisuals.animation.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.animation.protocol.social.SocialCommandWire;

public final class SocialCommandPayload {
    private SocialCommandPayload(){}
    public record Request(SocialCommandWire.Request value) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(Identifier.parse(SocialCommandWire.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Request> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(SocialCommandWire.encode(value.value)),buffer->new Request(SocialCommandWire.request(read(buffer,SocialCommandWire.MAX_REQUEST))));
        @Override public Type<Request> type(){return TYPE;}
    }
    public record Reply(SocialCommandWire.Reply value) implements CustomPacketPayload {
        public static final Type<Reply> TYPE=new Type<>(Identifier.parse(SocialCommandWire.REPLY_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Reply> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(SocialCommandWire.encode(value.value)),buffer->new Reply(SocialCommandWire.reply(read(buffer,SocialCommandWire.MAX_REPLY))));
        @Override public Type<Reply> type(){return TYPE;}
    }
    private static byte[] read(FriendlyByteBuf buffer,int maximum){int size=buffer.readableBytes();if(size<1||size>maximum)throw new DecoderException("Invalid social command length");byte[] bytes=new byte[size];buffer.readBytes(bytes);return bytes;}
}

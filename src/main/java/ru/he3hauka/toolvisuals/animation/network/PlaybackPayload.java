package ru.he3hauka.toolvisuals.animation.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire;

public final class PlaybackPayload {
    private PlaybackPayload(){}
    public record Request(PlaybackWire.Request value) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(Identifier.parse(PlaybackWire.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Request> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(PlaybackWire.encode(value.value)),buffer->{
            try{return new Request(PlaybackWire.decodeRequest(read(buffer,PlaybackWire.MAX_REQUEST_BYTES)));}
            catch(RuntimeException error){throw new DecoderException("Invalid playback request",error);}
        });
        @Override public Type<Request> type(){return TYPE;}
    }
    public record State(PlaybackWire.State value) implements CustomPacketPayload {
        public static final Type<State> TYPE=new Type<>(Identifier.parse(PlaybackWire.STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,State> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(PlaybackWire.encode(value.value)),buffer->{
            try{return new State(PlaybackWire.decodeState(read(buffer,PlaybackWire.MAX_STATE_BYTES)));}
            catch(RuntimeException error){throw new DecoderException("Invalid playback state",error);}
        });
        @Override public Type<State> type(){return TYPE;}
    }
    public record Reply(PlaybackWire.Reply value) implements CustomPacketPayload {
        public static final Type<Reply> TYPE=new Type<>(Identifier.parse(PlaybackWire.REPLY_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Reply> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(PlaybackWire.encode(value.value)),buffer->{
            try{return new Reply(PlaybackWire.decodeReply(read(buffer,PlaybackWire.MAX_REPLY_BYTES)));}
            catch(RuntimeException error){throw new DecoderException("Invalid playback reply",error);}
        });
        @Override public Type<Reply> type(){return TYPE;}
    }
    private static byte[] read(FriendlyByteBuf buffer,int maximum){
        int size=buffer.readableBytes();if(size<1||size>maximum)throw new DecoderException("Invalid playback payload length");
        byte[] bytes=new byte[size];buffer.readBytes(bytes);return bytes;
    }
}

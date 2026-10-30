package ru.he3hauka.toolvisuals.client.social.presence.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.network.presence.PresenceWire;

public final class PresencePayload {
    private PresencePayload(){}
    public record Request(PresenceWire.Subscription value) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(Identifier.parse(PresenceWire.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Request> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(PresenceWire.subscription(value.value)),buffer->{
            try{return new Request(PresenceWire.subscription(read(buffer,1)));}
            catch(RuntimeException error){throw new DecoderException("Invalid client list subscription",error);}
        });
        @Override public Type<Request> type(){return TYPE;}
    }
    public record State(PresenceWire.Page value) implements CustomPacketPayload {
        public static final Type<State> TYPE=new Type<>(Identifier.parse(PresenceWire.STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,State> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(PresenceWire.encode(value.value)),buffer->{
            try{return new State(PresenceWire.decode(read(buffer,PresenceWire.MAX_BYTES)));}
            catch(RuntimeException error){throw new DecoderException("Invalid client list state",error);}
        });
        @Override public Type<State> type(){return TYPE;}
    }
    private static byte[] read(FriendlyByteBuf buffer,int maximum){
        int size=buffer.readableBytes();if(size<1||size>maximum)throw new DecoderException("Invalid client list payload length");
        byte[] bytes=new byte[size];buffer.readBytes(bytes);return bytes;
    }
}

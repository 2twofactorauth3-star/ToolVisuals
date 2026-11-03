package ru.he3hauka.toolvisuals.client.friends.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ru.he3hauka.toolvisuals.network.friends.FriendWire;

public final class FriendPayload {
    public record Request(FriendWire.Request value) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(Identifier.parse(FriendWire.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Request> CODEC=StreamCodec.of((out,value)->out.writeBytes(FriendWire.encode(value.value)),in->new Request(FriendWire.request(read(in,FriendWire.MAX_REQUEST))));
        @Override public Type<Request> type(){return TYPE;}
    }
    public record State(FriendWire.State value) implements CustomPacketPayload {
        public static final Type<State> TYPE=new Type<>(Identifier.parse(FriendWire.STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,State> CODEC=StreamCodec.of((out,value)->out.writeBytes(FriendWire.encode(value.value)),in->new State(FriendWire.state(read(in,FriendWire.MAX_STATE))));
        @Override public Type<State> type(){return TYPE;}
    }
    private static byte[] read(FriendlyByteBuf in,int maximum){int size=in.readableBytes();if(size<1||size>maximum)throw new io.netty.handler.codec.DecoderException("Invalid friend HUD packet size");byte[] bytes=new byte[size];in.readBytes(bytes);return bytes;}
    private FriendPayload(){}
}

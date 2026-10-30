package ru.he3hauka.toolvisuals.client.cosmetic.accessory.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryWire;

public final class AccessoryPayload {
    private AccessoryPayload(){}
    public record Request(AccessoryWire.Request value) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(Identifier.parse(AccessoryWire.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Request> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(AccessoryWire.encode(value.value)),buffer->{try{return new Request(AccessoryWire.request(read(buffer,AccessoryWire.MAX_REQUEST)));}catch(RuntimeException error){throw new DecoderException("Invalid accessory request",error);}});
        @Override public Type<Request> type(){return TYPE;}
    }
    public record State(AccessoryWire.State value) implements CustomPacketPayload {
        public static final Type<State> TYPE=new Type<>(Identifier.parse(AccessoryWire.STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,State> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(AccessoryWire.encode(value.value)),buffer->{try{return new State(AccessoryWire.state(read(buffer,AccessoryWire.MAX_STATE)));}catch(RuntimeException error){throw new DecoderException("Invalid accessory state",error);}});
        @Override public Type<State> type(){return TYPE;}
    }
    public record Ack(AccessoryWire.Ack value) implements CustomPacketPayload {
        public static final Type<Ack> TYPE=new Type<>(Identifier.parse(AccessoryWire.ACK_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Ack> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(AccessoryWire.encode(value.value)),buffer->{try{return new Ack(AccessoryWire.ack(read(buffer,AccessoryWire.MAX_ACK)));}catch(RuntimeException error){throw new DecoderException("Invalid accessory acknowledgement",error);}});
        @Override public Type<Ack> type(){return TYPE;}
    }
    public record ExpandedState(AccessoryWire.State value) implements CustomPacketPayload {
        public static final Type<ExpandedState> TYPE=new Type<>(Identifier.parse(AccessoryWire.EXPANDED_STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,ExpandedState> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(AccessoryWire.encode(value.value)),buffer->new ExpandedState(State.CODEC.decode(buffer).value()));
        @Override public Type<ExpandedState> type(){return TYPE;}
    }
    private static byte[] read(FriendlyByteBuf buffer,int max){int size=buffer.readableBytes();if(size<1||size>max)throw new DecoderException("Invalid accessory payload size");byte[] bytes=new byte[size];buffer.readBytes(bytes);return bytes;}
}

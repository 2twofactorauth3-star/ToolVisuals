package ru.he3hauka.toolvisuals.client.cape.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.cosmetic.cape.CapeWire;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeTransferWire;

public final class CapePayload {
    private CapePayload(){}
    public record FragmentRequest(CapeTransferWire.RequestPart value) implements CustomPacketPayload {
        public static final Type<FragmentRequest> TYPE=new Type<>(Identifier.parse(CapeTransferWire.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,FragmentRequest> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(CapeTransferWire.encode(value.value)),buffer->{try{return new FragmentRequest(CapeTransferWire.request(read(buffer,CapeTransferWire.MAX_REQUEST)));}catch(RuntimeException error){throw new DecoderException("Invalid cape request fragment",error);}});
        @Override public Type<FragmentRequest> type(){return TYPE;}
    }
    public record FragmentState(CapeTransferWire.StatePart value) implements CustomPacketPayload {
        public static final Type<FragmentState> TYPE=new Type<>(Identifier.parse(CapeTransferWire.STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,FragmentState> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(CapeTransferWire.encode(value.value)),buffer->{try{return new FragmentState(CapeTransferWire.state(read(buffer,CapeTransferWire.MAX_STATE)));}catch(RuntimeException error){throw new DecoderException("Invalid cape state fragment",error);}});
        @Override public Type<FragmentState> type(){return TYPE;}
    }
    public record AnimatedRequest(CapeWire.Request value) implements CustomPacketPayload {
        public static final Type<AnimatedRequest> TYPE=new Type<>(Identifier.parse(CapeWire.ANIMATED_REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,AnimatedRequest> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(CapeWire.encodeAnimated(value.value)),buffer->{try{return new AnimatedRequest(CapeWire.animatedRequest(read(buffer,CapeWire.MAX_ANIMATED_REQUEST)));}catch(RuntimeException error){throw new DecoderException("Invalid animated cape request",error);}});
        @Override public Type<AnimatedRequest> type(){return TYPE;}
    }
    public record AnimatedState(CapeWire.State value) implements CustomPacketPayload {
        public static final Type<AnimatedState> TYPE=new Type<>(Identifier.parse(CapeWire.ANIMATED_STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,AnimatedState> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(CapeWire.encodeAnimated(value.value)),buffer->{try{return new AnimatedState(CapeWire.animatedState(read(buffer,CapeWire.MAX_ANIMATED_STATE)));}catch(RuntimeException error){throw new DecoderException("Invalid animated cape state",error);}});
        @Override public Type<AnimatedState> type(){return TYPE;}
    }
    public record Request(CapeWire.Request value) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(Identifier.parse(CapeWire.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Request> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(CapeWire.encode(value.value)),buffer->{try{return new Request(CapeWire.request(read(buffer,CapeWire.MAX_REQUEST)));}catch(RuntimeException error){throw new DecoderException("Invalid cape request",error);}});
        @Override public Type<Request> type(){return TYPE;}
    }
    public record State(CapeWire.State value) implements CustomPacketPayload {
        public static final Type<State> TYPE=new Type<>(Identifier.parse(CapeWire.STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,State> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(CapeWire.encode(value.value)),buffer->{try{return new State(CapeWire.state(read(buffer,CapeWire.MAX_STATE)));}catch(RuntimeException error){throw new DecoderException("Invalid cape state",error);}});
        @Override public Type<State> type(){return TYPE;}
    }
    public record Ack(CapeWire.Ack value) implements CustomPacketPayload {
        public static final Type<Ack> TYPE=new Type<>(Identifier.parse(CapeWire.ACK_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf,Ack> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(CapeWire.encode(value.value)),buffer->{try{return new Ack(CapeWire.ack(read(buffer,CapeWire.MAX_ACK)));}catch(RuntimeException error){throw new DecoderException("Invalid cape acknowledgement",error);}});
        @Override public Type<Ack> type(){return TYPE;}
    }
    private static byte[] read(FriendlyByteBuf buffer,int max){int size=buffer.readableBytes();if(size<1||size>max)throw new DecoderException("Invalid cape payload size");byte[] bytes=new byte[size];buffer.readBytes(bytes);return bytes;}
}

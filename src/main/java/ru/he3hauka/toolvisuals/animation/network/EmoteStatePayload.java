package ru.he3hauka.toolvisuals.animation.network;

import java.util.Objects;
import java.util.UUID;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EmoteStatePayload(UUID player,int emote,int age,boolean extended) implements CustomPacketPayload {
    public static final Type<EmoteStatePayload> TYPE=new Type<>(Identifier.parse(EmoteWire.STATE_CHANNEL));
    public static final Type<EmoteStatePayload> EXTENDED_TYPE=new Type<>(Identifier.parse(EmoteWire.EXTENDED_STATE_CHANNEL));
    public EmoteStatePayload(UUID player,int emote,int age){this(player,emote,age,false);}
    public EmoteStatePayload {
        Objects.requireNonNull(player);
        if(!EmoteWire.validState(emote,age))throw new IllegalArgumentException("Invalid emote state");
    }
    public static final StreamCodec<FriendlyByteBuf,EmoteStatePayload> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(EmoteWire.encode(new EmoteWire.State(value.player,value.emote,value.age))),EmoteStatePayload::decode);
    public static final StreamCodec<FriendlyByteBuf,EmoteStatePayload> EXTENDED_CODEC=CODEC.map(value->new EmoteStatePayload(value.player,value.emote,value.age,true),value->value);
    private static EmoteStatePayload decode(FriendlyByteBuf buffer) {
        int length=buffer.readableBytes();
        if(length<18||length>EmoteWire.MAX_STATE_BYTES)throw new DecoderException("Invalid emote state size");
        byte[] bytes=new byte[length];buffer.readBytes(bytes);
        try{var state=EmoteWire.decodeState(bytes);return new EmoteStatePayload(state.player(),state.emote(),state.age());}
        catch(RuntimeException error){throw new DecoderException("Invalid emote state",error);}
    }
    @Override public Type<EmoteStatePayload> type(){return extended?EXTENDED_TYPE:TYPE;}
}

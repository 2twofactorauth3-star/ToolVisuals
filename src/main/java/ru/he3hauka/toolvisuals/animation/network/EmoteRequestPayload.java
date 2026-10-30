package ru.he3hauka.toolvisuals.animation.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EmoteRequestPayload(int emote,boolean cancelMoving,boolean extended) implements CustomPacketPayload {
    public static final Type<EmoteRequestPayload> TYPE=new Type<>(Identifier.parse(EmoteWire.REQUEST_CHANNEL));
    public static final Type<EmoteRequestPayload> EXTENDED_TYPE=new Type<>(Identifier.parse(EmoteWire.EXTENDED_REQUEST_CHANNEL));
    public EmoteRequestPayload(int emote,boolean cancelMoving){this(emote,cancelMoving,false);}
    public EmoteRequestPayload {
        if(!EmoteWire.validRequest(emote))throw new IllegalArgumentException("Unknown emote");
    }
    public static final StreamCodec<FriendlyByteBuf,EmoteRequestPayload> CODEC=StreamCodec.of((buffer,value)->buffer.writeBytes(EmoteWire.encode(new EmoteWire.Request(value.emote,value.cancelMoving))),EmoteRequestPayload::decode);
    public static final StreamCodec<FriendlyByteBuf,EmoteRequestPayload> EXTENDED_CODEC=CODEC.map(value->new EmoteRequestPayload(value.emote,value.cancelMoving,true),value->value);
    private static EmoteRequestPayload decode(FriendlyByteBuf buffer) {
        int length=buffer.readableBytes();
        if(length<2||length>EmoteWire.MAX_REQUEST_BYTES)throw new DecoderException("Invalid emote request size");
        byte[] bytes=new byte[length];buffer.readBytes(bytes);
        try{var request=EmoteWire.decodeRequest(bytes);return new EmoteRequestPayload(request.emote(),request.cancelMoving());}
        catch(RuntimeException error){throw new DecoderException("Invalid emote request",error);}
    }
    @Override public Type<EmoteRequestPayload> type(){return extended?EXTENDED_TYPE:TYPE;}
}

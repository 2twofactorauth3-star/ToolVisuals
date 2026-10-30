package ru.he3hauka.toolvisuals.animation.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.animation.social.SocialProtocol;

public final class SocialPayload {
    private SocialPayload() {}
    public record Request(SocialProtocol.Request value) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(Identifier.parse(SocialProtocol.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf, Request> CODEC = StreamCodec.of((buffer, value) -> buffer.writeBytes(SocialProtocol.encode(value.value)), buffer -> {
            try { return new Request(SocialProtocol.decodeRequest(read(buffer, SocialProtocol.MAX_REQUEST_BYTES))); }
            catch (RuntimeException error) { throw new DecoderException("Invalid ToolVisuals social request", error); }
        });
        @Override public Type<Request> type() { return TYPE; }
    }
    public record State(SocialProtocol.State value) implements CustomPacketPayload {
        public static final Type<State> TYPE = new Type<>(Identifier.parse(SocialProtocol.STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf, State> CODEC = StreamCodec.of((buffer, value) -> buffer.writeBytes(SocialProtocol.encode(value.value)), buffer -> {
            try { return new State(SocialProtocol.decodeState(read(buffer, SocialProtocol.MAX_STATE_BYTES))); }
            catch (RuntimeException error) { throw new DecoderException("Invalid ToolVisuals social state", error); }
        });
        @Override public Type<State> type() { return TYPE; }
    }
    private static byte[] read(FriendlyByteBuf buffer, int maximum) {
        int length = buffer.readableBytes();
        if (length < 1 || length > maximum) throw new DecoderException("Invalid ToolVisuals social payload size");
        byte[] bytes = new byte[length]; buffer.readBytes(bytes); return bytes;
    }
}

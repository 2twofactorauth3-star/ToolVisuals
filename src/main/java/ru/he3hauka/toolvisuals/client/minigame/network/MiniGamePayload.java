package ru.he3hauka.toolvisuals.client.minigame.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.minigame.network.MiniGameWire;

public final class MiniGamePayload {
    private MiniGamePayload() {}
    public record Request(MiniGameWire.Command value) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(Identifier.parse(MiniGameWire.REQUEST_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf, Request> CODEC = StreamCodec.of((buffer, value) -> buffer.writeBytes(MiniGameWire.encode(value.value())), buffer -> {
            try { return new Request(MiniGameWire.command(read(buffer, MiniGameWire.MAX_REQUEST))); }
            catch (IllegalArgumentException invalid) { throw new DecoderException("Invalid minigame request", invalid); }
        });
        @Override public Type<Request> type() { return TYPE; }
    }
    public record State(MiniGameWire.Message value) implements CustomPacketPayload {
        public static final Type<State> TYPE = new Type<>(Identifier.parse(MiniGameWire.STATE_CHANNEL));
        public static final StreamCodec<FriendlyByteBuf, State> CODEC = StreamCodec.of((buffer, value) -> buffer.writeBytes(MiniGameWire.encode(value.value())), buffer -> {
            try { return new State(MiniGameWire.message(read(buffer, MiniGameWire.MAX_STATE))); }
            catch (IllegalArgumentException invalid) { throw new DecoderException("Invalid minigame state", invalid); }
        });
        @Override public Type<State> type() { return TYPE; }
    }
    private static byte[] read(FriendlyByteBuf buffer, int maximum) {
        int size = buffer.readableBytes(); if (size <= 0 || size > maximum) throw new DecoderException("Invalid minigame payload size");
        byte[] bytes = new byte[size]; buffer.readBytes(bytes); return bytes;
    }
}

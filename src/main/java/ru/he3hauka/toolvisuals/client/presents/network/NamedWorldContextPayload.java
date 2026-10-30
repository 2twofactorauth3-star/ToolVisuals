package ru.he3hauka.toolvisuals.client.presents.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire;

public record NamedWorldContextPayload(WorldContextWire.State value) implements WorldContextMessage {
    public static final Type<NamedWorldContextPayload> TYPE = new Type<>(Identifier.parse(WorldContextWire.NAMED_CHANNEL));
    public static final StreamCodec<FriendlyByteBuf, NamedWorldContextPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeBytes(WorldContextWire.encodeNamed(payload.value())), buffer -> {
                int length = buffer.readableBytes();
                if (length < 20 || length > WorldContextWire.MAX_NAMED_BYTES) throw new DecoderException("Invalid named world context size");
                byte[] bytes = new byte[length];
                buffer.readBytes(bytes);
                try { return new NamedWorldContextPayload(WorldContextWire.decodeNamed(bytes)); }
                catch (IllegalArgumentException error) { throw new DecoderException("Invalid named world context", error); }
            });

    @Override public Type<NamedWorldContextPayload> type() { return TYPE; }
}

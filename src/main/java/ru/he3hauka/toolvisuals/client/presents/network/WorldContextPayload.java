package ru.he3hauka.toolvisuals.client.presents.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire;
import java.util.function.Function;

public record WorldContextPayload(WorldContextWire.State value) implements WorldContextMessage {
    public static final Type<WorldContextPayload> TYPE = new Type<>(Identifier.parse(WorldContextWire.CHANNEL));
    public static final StreamCodec<FriendlyByteBuf, WorldContextPayload> CODEC = codec(false, WorldContextPayload::new);

    static <T extends WorldContextMessage> StreamCodec<FriendlyByteBuf, T> codec(boolean extended, Function<WorldContextWire.State, T> factory) {
        return StreamCodec.of((buffer, payload) -> buffer.writeBytes(extended
                ? WorldContextWire.encodeExtended(payload.value()) : WorldContextWire.encode(payload.value())), buffer -> {
                if (buffer.readableBytes() != WorldContextWire.BYTES) throw new DecoderException("Invalid world context size");
                var bytes = new byte[WorldContextWire.BYTES]; buffer.readBytes(bytes);
                try { return factory.apply(extended ? WorldContextWire.decodeExtended(bytes) : WorldContextWire.decode(bytes)); }
                catch (IllegalArgumentException error) { throw new DecoderException("Invalid world context", error); }
            });
    }

    @Override public Type<WorldContextPayload> type() { return TYPE; }
}

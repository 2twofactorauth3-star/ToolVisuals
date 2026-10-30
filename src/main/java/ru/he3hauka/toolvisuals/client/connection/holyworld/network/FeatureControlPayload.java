package ru.he3hauka.toolvisuals.client.connection.holyworld.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.client.connection.holyworld.HolyWorldProtocol;

public record FeatureControlPayload(byte[] bytes) implements CustomPacketPayload {
    public static final Type<FeatureControlPayload> TYPE = new Type<>(Identifier.parse(HolyWorldProtocol.CHANNEL));
    public static final StreamCodec<FriendlyByteBuf, FeatureControlPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeBytes(payload.bytes), FeatureControlPayload::decode);

    public FeatureControlPayload {
        if (bytes.length > HolyWorldProtocol.MAX_BYTES) throw new IllegalArgumentException("Feature payload exceeds its budget");
        bytes = bytes.clone();
    }

    private static FeatureControlPayload decode(FriendlyByteBuf buffer) {
        int size = buffer.readableBytes();
        if (size > HolyWorldProtocol.MAX_BYTES) {
            buffer.skipBytes(size);
            return new FeatureControlPayload(new byte[0]);
        }
        var bytes = new byte[size];
        buffer.readBytes(bytes);
        return new FeatureControlPayload(bytes);
    }

    @Override public Type<FeatureControlPayload> type() { return TYPE; }
}

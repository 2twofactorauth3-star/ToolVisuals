package ru.he3hauka.toolvisuals.client.presents.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire;

public record ExtendedWorldContextPayload(WorldContextWire.State value) implements WorldContextMessage {
    public static final Type<ExtendedWorldContextPayload> TYPE = new Type<>(Identifier.parse(WorldContextWire.EXTENDED_CHANNEL));
    public static final StreamCodec<FriendlyByteBuf, ExtendedWorldContextPayload> CODEC = WorldContextPayload.codec(true, ExtendedWorldContextPayload::new);

    @Override public Type<ExtendedWorldContextPayload> type() { return TYPE; }
}

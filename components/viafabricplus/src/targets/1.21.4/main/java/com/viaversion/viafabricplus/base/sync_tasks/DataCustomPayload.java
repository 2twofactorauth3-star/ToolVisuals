package com.viaversion.viafabricplus.base.sync_tasks;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public record DataCustomPayload(PacketByteBuf buf) implements CustomPayload {

    public static final CustomPayload.Id<DataCustomPayload> ID = new CustomPayload.Id<>(Identifier.of(SyncTasks.PACKET_SYNC_IDENTIFIER));

    static {
        PayloadTypeRegistry.playS2C().register(DataCustomPayload.ID, CustomPayload.codecOf((value, buf) -> {
            throw new UnsupportedOperationException("DataCustomPayload is a read-only packet");
        }, buf -> new DataCustomPayload(new PacketByteBuf(Unpooled.copiedBuffer(buf.readSlice(buf.readableBytes()))))));
    }

    public static void init() {

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}

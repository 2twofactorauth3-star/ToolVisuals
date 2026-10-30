package com.viaversion.viafabricplus.base.sync_tasks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class SyncTasks {

    private static final Map<String, Consumer<RegistryByteBuf>> PENDING_EXECUTION_TASKS = new ConcurrentHashMap<>();

    public static final String PACKET_SYNC_IDENTIFIER = UUID.randomUUID() + ":" + UUID.randomUUID();

    static {
        DataCustomPayload.init();
    }

    public static void init() {

    }

    public static String executeSyncTask(final Consumer<RegistryByteBuf> task) {
        final String uuid = UUID.randomUUID().toString();
        PENDING_EXECUTION_TASKS.put(uuid, task);
        return uuid;
    }

    public static void handleSyncTask(final PacketByteBuf buf) {
        final String uuid = buf.readString();

        if (PENDING_EXECUTION_TASKS.containsKey(uuid)) {
            MinecraftClient.getInstance().execute(() -> {
                final Consumer<RegistryByteBuf> task = PENDING_EXECUTION_TASKS.remove(uuid);
                task.accept(new RegistryByteBuf(buf, MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()));
            });
        }
    }

}

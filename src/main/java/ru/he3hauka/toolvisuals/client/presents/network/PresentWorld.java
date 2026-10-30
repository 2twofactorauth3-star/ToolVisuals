package ru.he3hauka.toolvisuals.client.presents.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import ru.he3hauka.toolvisuals.client.presents.PresentCatalog;
import ru.he3hauka.toolvisuals.client.waypoint.Waypoints;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire;

public final class PresentWorld {
    private static ClientLevel level;
    private static ClientPacketListener connection;
    private static WorldContextWire.State state;
    private static boolean extended;
    private static int version;

    private PresentWorld() {}

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(WorldContextPayload.TYPE, WorldContextPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ExtendedWorldContextPayload.TYPE, ExtendedWorldContextPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(NamedWorldContextPayload.TYPE, NamedWorldContextPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(WorldContextPayload.TYPE, (payload, context) -> accept(context.client(), payload.value(), 1));
        ClientPlayNetworking.registerGlobalReceiver(ExtendedWorldContextPayload.TYPE, (payload, context) -> accept(context.client(), payload.value(), 2));
        ClientPlayNetworking.registerGlobalReceiver(NamedWorldContextPayload.TYPE, (payload, context) -> accept(context.client(), payload.value(), 3));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
            if (connection == handler) { level = null; connection = null; state = null; extended = false; version = 0; }
        }));
    }

    private static void accept(Minecraft client, WorldContextWire.State value, int incomingVersion) {
        if (incomingVersion < version && client.getConnection() == connection && client.level == level) return;
        level = client.level;
        connection = client.getConnection();
        state = value;
        version = incomingVersion;
        extended = incomingVersion >= 2;
    }

    public static WorldContextWire.State identity(Minecraft client) {
        return client.level != null && client.level == level && client.getConnection() == connection ? state : null;
    }

    public static WorldContextWire.Area area(Minecraft client) {
        if (client.level == null) return WorldContextWire.Area.OTHER;
        if (client.getConnection() == connection && client.level == level && state != null
                && (extended || state.area() != WorldContextWire.Area.OTHER)) return state.area();
        return PresentCatalog.area(Waypoints.dimension());
    }
}

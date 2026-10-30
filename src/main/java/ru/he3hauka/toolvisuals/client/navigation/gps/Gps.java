package ru.he3hauka.toolvisuals.client.navigation.gps;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.presents.network.PresentWorld;
import ru.he3hauka.toolvisuals.client.waypoint.Waypoints;

public final class Gps {
    private static Object connection;
    private static GpsTarget target;

    private Gps() {}

    public static void command(String arguments) {
        if (arguments.strip().equalsIgnoreCase("off")) {
            target = null;
            connection = null;
            ChatMessages.notice("gps.stopped");
            return;
        }
        var client = Minecraft.getInstance();
        if (client.player == null || client.level == null) { ChatMessages.notice("gps.no_world"); return; }
        try {
            var next = GpsTarget.parse(arguments);
            connection = client.getConnection();
            target = next;
            ChatMessages.notice(next.world().isEmpty() ? "gps.started" : "gps.started_world",
                    next.x(), next.y(), next.z(), next.world());
        } catch (IllegalArgumentException error) { ChatMessages.notice("gps.usage"); }
    }

    public static void tick(Minecraft client) {
        if (connection != client.getConnection()) { target = null; connection = null; }
    }

    public static GpsTarget visible(Minecraft client) {
        var current = target;
        if (current == null || connection != client.getConnection() || client.level == null || client.player == null) return null;
        var identity = PresentWorld.identity(client);
        String name = identity == null ? "" : identity.name();
        if (name.isEmpty() && client.getSingleplayerServer() != null)
            name = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
        if (name.isEmpty() && identity != null && identity.area() != ru.he3hauka.toolvisuals.network.world.WorldContextWire.Area.OTHER)
            name = identity.area().name();
        return current.matches(Waypoints.dimension(), name, identity == null ? null : identity.world()) ? current : null;
    }
}

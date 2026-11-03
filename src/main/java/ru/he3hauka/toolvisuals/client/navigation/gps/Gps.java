package ru.he3hauka.toolvisuals.client.navigation.gps;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.navigation.gps.ui.GpsScreen;
import ru.he3hauka.toolvisuals.client.presents.network.PresentWorld;
import ru.he3hauka.toolvisuals.client.waypoint.Waypoints;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.GPS;

public final class Gps {
    private static volatile List<GpsMark> visible = List.of();
    private static boolean openRequested;
    private static Object visibleConnection, visibleLevel;
    private Gps() {}

    public static void initialize() {
        try { ToolVisualsClient.GPS_POINTS.load(); }
        catch (IOException error) { ClientErrors.report("Load GPS markers; original file preserved", error); }
    }

    public static void command(String arguments) {
        if (!ToolVisualsClient.CONFIG.active(GPS)) { ChatMessages.notice("gps.disabled"); return; }
        if (arguments.isBlank() || arguments.strip().equalsIgnoreCase("list")) { openRequested = true; return; }
        String context = Waypoints.context();
        if (context.isEmpty() || Minecraft.getInstance().player == null) { ChatMessages.notice("gps.no_world"); return; }
        try {
            if (arguments.strip().equalsIgnoreCase("off")) {
                ToolVisualsClient.GPS_POINTS.selectAll(context, false);
                ChatMessages.notice("gps.stopped");
            } else {
                var target = GpsTarget.parse(arguments);
                var old = ToolVisualsClient.GPS_POINTS.entries(context).stream().filter(mark -> mark.target().equals(target)).findFirst().orElse(null);
                var mark = old == null ? new GpsMark(UUID.randomUUID(), context,
                        "GPS " + (ToolVisualsClient.GPS_POINTS.entries(context).size() + 1), target, true) : old.active(true);
                ToolVisualsClient.GPS_POINTS.put(mark, true);
                ChatMessages.notice(target.world().isEmpty() ? "gps.started" : "gps.started_world", target.x(), target.y(), target.z(), target.world());
            }
            tick(Minecraft.getInstance());
        } catch (IOException error) { ChatMessages.notice("gps.save_error"); }
        catch (IllegalArgumentException error) { ChatMessages.notice("gps.usage"); }
        catch (IllegalStateException error) { ChatMessages.notice("gps.limit"); }
    }

    public static void tick(Minecraft client) {
        visibleConnection=client.getConnection();visibleLevel=client.level;
        if (openRequested) {
            openRequested = false;
            if (ToolVisualsClient.CONFIG.active(GPS)) client.gui.setScreen(new GpsScreen(null));
        }
        if (!ToolVisualsClient.CONFIG.active(GPS) || client.level == null || client.player == null) { visible = List.of(); return; }
        var identity = PresentWorld.identity(client);
        String world = identity == null ? "" : identity.name();
        if (world.isEmpty() && client.getSingleplayerServer() != null)
            world = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
        if (world.isEmpty() && identity != null && identity.area() != ru.he3hauka.toolvisuals.network.world.WorldContextWire.Area.OTHER)
            world = identity.area().name();
        String name = world;
        visible = ToolVisualsClient.GPS_POINTS.entries(Waypoints.context()).stream().filter(GpsMark::active)
                .filter(mark -> mark.target().matches(Waypoints.dimension(), name, identity == null ? null : identity.world())).toList();
    }

    public static List<GpsMark> visibleMarkers(Minecraft client) {
        return client.level != null && client.level==visibleLevel && client.getConnection()==visibleConnection && client.player != null && ToolVisualsClient.CONFIG.active(GPS) ? visible : List.of();
    }
}

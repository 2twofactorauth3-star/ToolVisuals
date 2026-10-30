package ru.he3hauka.toolvisuals.client.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.player.ClientActions;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.WAYPOINTS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.WAYPOINT_DEATH;

public final class Waypoints {
    private static Object connection;
    private static boolean alive;
    private static String context="";
    private Waypoints() {}
    public static String context() { return context; }
    public static String dimension() { var level=Minecraft.getInstance().level;return level==null?"":level.dimension().identifier().toString(); }
    public static void tick(Minecraft client) {
        if(connection!=client.getConnection()) {
            connection=client.getConnection();alive=false;context="";
            if(connection!=null) {
                var server=client.getSingleplayerServer();var remote=client.getCurrentServer();
                if(server!=null)context="world:"+server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
                else if(remote!=null)context="server:"+remote.ip.strip().toLowerCase(java.util.Locale.ROOT);
            }
        }
        if(client.player==null||client.level==null)return;
        boolean current=client.player.isAlive();
        if(alive&&!current&&!context.isEmpty()&&ToolVisualsClient.CONFIG.active(WAYPOINTS)&&ToolVisualsClient.CONFIG.flag(WAYPOINT_DEATH)) {
            try {
                var p=client.player;ToolVisualsClient.WAYPOINTS.death(context,dimension(),p.getX(),p.getY(),p.getZ());
                ClientActions.notice("waypoints.death_saved");
            } catch(java.io.IOException|IllegalArgumentException|IllegalStateException error) {
                ClientActions.notice("waypoints.save_error");
            }
        }
        alive=current;
    }
}

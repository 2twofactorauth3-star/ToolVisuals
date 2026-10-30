package ru.he3hauka.toolvisuals.client.zoom;

import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.ZOOM;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class VisualZoom {
    private static final TimedZoom amount=new TimedZoom();
    private static Object world;
    private VisualZoom() {}

    public static float fov(float original) {
        var client = Minecraft.getInstance();
        var config = ToolVisualsClient.CONFIG;
        long now = System.nanoTime();
        if(world!=client.level){world=client.level;amount.reset();}
        boolean held = config.active(ZOOM) && ToolVisualsClient.zoomKey != null && ToolVisualsClient.zoomKey.isDown()
                && client.player != null && client.gui.screen() == null && client.isWindowActive();
        ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.state(ZOOM,"input",held?"zoom_held":"zoom_released_or_unavailable");
        double distance=config.value(ZOOM_RANGE_MODE)==0?client.options.getEffectiveRenderDistance()*16:config.value(ZOOM_DISTANCE);
        double factor=config.value(ZOOM_RANGE_MODE)==2?config.value(ZOOM_FACTOR):TimedZoom.factor(distance);
        return original*amount.sample(now,held,config.value(ZOOM_DURATION),factor,config.flag(ZOOM_SMOOTH));
    }

    public static float sensitivity() { return amount.value(); }
}

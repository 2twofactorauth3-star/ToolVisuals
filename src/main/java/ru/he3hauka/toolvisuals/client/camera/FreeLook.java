package ru.he3hauka.toolvisuals.client.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Team;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.FREE_LOOK;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.FREE_LOOK_MODE;

public final class FreeLook {
    private static LocalPlayer owner;
    private static int mode = -1;
    private static boolean toggled;
    private static boolean keyWasDown;
    private static float yaw, pitch;
    private FreeLook() {}
    public static boolean active() { return owner != null && !ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active(); }
    public static float yaw() { return yaw; }
    public static float pitch() { return pitch; }
    public static boolean owns(Entity entity) { return owner != null && entity == owner; }
    public static boolean showsOwnLabels(Entity entity) {
        if (!owns(entity) || !ToolVisualsClient.CONFIG.flag(ru.he3hauka.toolvisuals.client.module.VisualSetting.FREE_LOOK_SELF_LABELS)
                || NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.NAME_TAGS)) return false;
        var team = owner.getTeam();
        return team == null || team.getNameTagVisibility() != Team.Visibility.NEVER
                && team.getNameTagVisibility() != Team.Visibility.HIDE_FOR_OWN_TEAM;
    }
    public static void update(Minecraft client) {
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;
        ru.he3hauka.toolvisuals.client.camera.ClientCamera.update(client);
        int nextMode = (int)ToolVisualsClient.CONFIG.value(FREE_LOOK_MODE);
        if (owner != client.player || mode != nextMode) { release(client); toggled = false; }
        mode = nextMode;
        boolean usable = ToolVisualsClient.CONFIG.active(FREE_LOOK) && client.player != null
                && client.level != null && client.gui.screen() == null && client.gui.overlay() == null
                && client.isWindowActive() && !client.player.isSleeping() && client.getCameraEntity() == client.player;
        boolean held = ToolVisualsClient.freeLookKey != null && ToolVisualsClient.freeLookKey.isDown();
        int clicks = 0;
        if (ToolVisualsClient.freeLookKey != null)
            while (ToolVisualsClient.freeLookKey.consumeClick()) clicks++;
         
        boolean pressed = held ? !keyWasDown : (clicks & 1) != 0;
        keyWasDown = held;
        if (usable && mode == 2 && pressed) toggled = !toggled;
        if (!usable) toggled = false;
        boolean requested = mode == 0 ? !client.options.getCameraType().isFirstPerson() : mode == 1 ? held : toggled;
        boolean allowed = usable && requested;
        if (active() && !allowed) release(client);
        if (allowed && !active()) {
            owner = client.player;
            ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(FREE_LOOK,"acquired","camera_control");
            yaw = owner.getYRot(); pitch = owner.getXRot();
            if (mode != 0) ru.he3hauka.toolvisuals.client.camera.ClientCamera.acquire(client, ru.he3hauka.toolvisuals.client.camera.CameraLeases.Owner.FREE_LOOK);
        }
    }
    public static boolean turn(double dx, double dy) {
        update(Minecraft.getInstance());
        if (!active()) return false;
        yaw = net.minecraft.util.Mth.wrapDegrees(yaw + (float)dx * .15f);
        pitch = Math.clamp(pitch + (float)dy * .15f, -90, 90);
        return true;
    }
    private static void release(Minecraft client) {
        if(owner!=null)ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(FREE_LOOK,"released","camera_control");
        ru.he3hauka.toolvisuals.client.camera.ClientCamera.release(client, ru.he3hauka.toolvisuals.client.camera.CameraLeases.Owner.FREE_LOOK);
        owner = null;
    }
}

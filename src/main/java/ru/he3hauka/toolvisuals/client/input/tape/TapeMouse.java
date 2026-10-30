package ru.he3hauka.toolvisuals.client.input.tape;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.input.VirtualKeyScope;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.TAPE_MOUSE;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class TapeMouse {
    private static final TapePlayback PLAYBACK = new TapePlayback();
    private static Object connection;
    private static Object player;
    private static Object level;

    private TapeMouse() {}

    public static void tick(Minecraft client) {
        while (ToolVisualsClient.tapeMouseKey.consumeClick()) {
            if (available(client) && client.isWindowActive())
                ToolVisualsClient.CONFIG.setActive(TAPE_MOUSE, !ToolVisualsClient.CONFIG.active(TAPE_MOUSE));
        }
        if (!ToolVisualsClient.CONFIG.active(TAPE_MOUSE)) { clear(); return; }
        if (connection != null && (connection != client.getConnection() || player != client.player || level != client.level
                || client.player.isDeadOrDying())) {
            ToolVisualsClient.CONFIG.setActive(TAPE_MOUSE, false);
            clear();
            return;
        }
        if (client.getConnection() != null && client.player != null && client.level != null) {
            connection = client.getConnection();
            player = client.player;
            level = client.level;
        }
        PLAYBACK.tick(TapeAction.at((int)ToolVisualsClient.CONFIG.value(TAPE_ACTION)),
                ToolVisualsClient.CONFIG.value(TAPE_INTERVAL), System.nanoTime(), available(client));
    }

    public static void clear() {
        PLAYBACK.clear();
        connection = player = level = null;
    }

    public static boolean down(Minecraft client, KeyMapping mapping, boolean physical) {
        return physical || VirtualKeyScope.virtualAllowed() && active(client) && PLAYBACK.down(button(client.options, mapping));
    }

    public static boolean consumeClick(Minecraft client, KeyMapping mapping, boolean physical) {
        return active(client) ? PLAYBACK.consumeClick(button(client.options, mapping), physical) : physical;
    }

    public static boolean holdingAttack(Minecraft client) {
        return active(client) && PLAYBACK.down(TapeAction.ATTACK.mask);
    }

    private static boolean active(Minecraft client) {
        return ToolVisualsClient.CONFIG.active(TAPE_MOUSE) && connection != null && connection == client.getConnection()
                && player == client.player && level == client.level && available(client);
    }

    private static boolean available(Minecraft client) {
        return client.player != null && client.level != null && client.gameMode != null && client.getConnection() != null
                && client.gui.screen() == null && client.gui.overlay() == null && !client.isPaused()
                && !client.player.isDeadOrDying() && !client.player.isSleeping() && !client.player.isSpectator()
                && !ReplayScope.active() && (client.isWindowActive() || ToolVisualsClient.CONFIG.flag(TAPE_BACKGROUND));
    }

    private static int button(Options options, KeyMapping mapping) {
        if (mapping == options.keyAttack) return TapeAction.ATTACK.mask;
        if (mapping == options.keyUse) return TapeAction.USE.mask;
        if (mapping == options.keyUp) return TapeAction.FORWARD.mask;
        if (mapping == options.keyDown) return TapeAction.BACKWARD.mask;
        if (mapping == options.keyLeft) return TapeAction.LEFT.mask;
        if (mapping == options.keyRight) return TapeAction.RIGHT.mask;
        if (mapping == options.keyJump) return TapeAction.JUMP.mask;
        if (mapping == options.keyShift) return TapeAction.SNEAK.mask;
        if (mapping == options.keySprint) return TapeAction.SPRINT.mask;
        return 0;
    }
}

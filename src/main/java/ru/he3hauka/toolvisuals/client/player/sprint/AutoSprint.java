package ru.he3hauka.toolvisuals.client.player.sprint;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog;
import ru.he3hauka.toolvisuals.client.hud.layout.HudEditorScreen;
import ru.he3hauka.toolvisuals.client.hud.layout.HudMovement;
import ru.he3hauka.toolvisuals.client.input.VirtualKeyScope;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

public final class AutoSprint {
    private AutoSprint() {}

    public static boolean down(Minecraft client, KeyMapping mapping, boolean original) {
        if (client == null || client.options == null || mapping != client.options.keySprint) return original;
        if(original)return result(true,"physical_sprint_input");
        if (!VirtualKeyScope.virtualAllowed()) return false;
        if (!available(client)) return result(false,"context_unavailable");
        if (!ToolVisualsClient.CONFIG.active(VisualModule.AUTO_SPRINT)) return result(false,"module_disabled");
        if(!movementDown(client, client.options.keyUp))return result(false,"forward_not_held");
        if(movementDown(client, client.options.keyDown))return result(false,"backward_held");
        if(movementDown(client, client.options.keyShift))return result(false,"sneaking");
        return result(true,"sprint_input_supplied");
    }

    private static boolean result(boolean value,String reason){ModuleLog.state(VisualModule.AUTO_SPRINT,"decision",reason);return value;}

    private static boolean available(Minecraft client) {
        return client.player != null && client.level != null && client.gui != null && client.gui.overlay() == null
                && client.isWindowActive() && !client.isPaused() && !client.player.isDeadOrDying() && !client.player.isSleeping()
                && !ReplayScope.active() && (client.gui.screen() == null || HudMovement.active(client));
    }

    private static boolean movementDown(Minecraft client, KeyMapping mapping) {
        return client.gui.screen() instanceof HudEditorScreen ? HudMovement.down(client, mapping) : mapping.isDown();
    }
}

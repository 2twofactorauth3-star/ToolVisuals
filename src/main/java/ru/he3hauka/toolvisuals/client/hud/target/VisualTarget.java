package ru.he3hauka.toolvisuals.client.hud.target;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.TARGET_HUD_HOLD;

public final class VisualTarget {
    private static ClientLevel level;
    private static LivingEntity target;
    private static long lastSeen;
    private VisualTarget() {}

    public static void tick(Minecraft client) {
        if (client.level != level || client.player == null) {
            level = client.level; target = null; lastSeen = 0;
        }
        if (client.player == null || level == null) return;
        long now = level.getGameTime();
        if (client.crosshairPickEntity instanceof LivingEntity living && living.isAlive()
                && living != client.player && !living.isInvisibleTo(client.player)) {
            target = living; lastSeen = now;
        }
        if (target != null && (!target.isAlive() || target.isRemoved() || target.level() != level
                || target.distanceToSqr(client.player) > 64 * 64
                || now - lastSeen > ToolVisualsClient.CONFIG.value(TARGET_HUD_HOLD) * 20)) target = null;
    }

    public static LivingEntity current() { return target; }
}

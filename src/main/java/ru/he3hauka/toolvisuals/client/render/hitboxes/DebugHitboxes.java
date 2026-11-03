package ru.he3hauka.toolvisuals.client.render.hitboxes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

public final class DebugHitboxes {
    private DebugHitboxes() {}
    public static boolean enabled(Minecraft client) {
        return client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES);
    }
}

package ru.he3hauka.toolvisuals.client.render.hitboxes;

import net.minecraft.client.Minecraft;

public final class DebugHitboxes {
    private DebugHitboxes() {}
    public static boolean enabled(Minecraft client) { return client.getEntityRenderDispatcher().shouldRenderHitBoxes(); }
}

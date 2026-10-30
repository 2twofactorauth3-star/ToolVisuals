package ru.he3hauka.toolvisuals.client.optimization.occlusion;

import net.minecraft.world.entity.Entity;

public final class ExtractionVisibility {
    private static final ThreadLocal<Entity> HIDDEN = new ThreadLocal<>();

    private ExtractionVisibility() {}
    public static Entity current() { return HIDDEN.get(); }
    public static boolean hidden(Entity entity) { return HIDDEN.get() == entity; }
    public static void restore(Entity entity) {
        HIDDEN.set(entity);
    }
}

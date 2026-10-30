package ru.he3hauka.toolvisuals.client.items.render;

import net.minecraft.world.phys.AABB;

public interface ItemBoundsState {
    AABB toolvisuals$bounds();
    void toolvisuals$bounds(AABB bounds);
}

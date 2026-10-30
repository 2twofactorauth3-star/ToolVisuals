package ru.he3hauka.toolvisuals.client.friends;

import net.minecraft.network.chat.Component;

public interface FriendRenderState {
    int toolvisuals$friendTint();void toolvisuals$friendTint(int value);
    int toolvisuals$friendBodyTint();void toolvisuals$friendBodyTint(int value);
    boolean toolvisuals$friendArmorFill();void toolvisuals$friendArmorFill(boolean value);
    Component toolvisuals$friendLabel();void toolvisuals$friendLabel(Component value);
}

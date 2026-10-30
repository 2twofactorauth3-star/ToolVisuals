package ru.he3hauka.toolvisuals.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class NoSwing {
    private NoSwing() {}
    public static boolean firstPerson() {
        var config = ToolVisualsClient.CONFIG;
        return config.active(NO_SWING_ANIMATION) && config.flag(SWING_FIRST_PERSON)
                || config.active(CUSTOM_HAND) && config.flag(NO_SWING);
    }
    public static boolean player(AvatarRenderState state) {
        var config = ToolVisualsClient.CONFIG;
        var player = Minecraft.getInstance().player;
        return config.active(NO_SWING_ANIMATION) && player != null
                && config.flag(state.id == player.getId() ? SWING_SELF : SWING_OTHERS);
    }
}

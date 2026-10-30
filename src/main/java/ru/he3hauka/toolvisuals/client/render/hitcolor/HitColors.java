package ru.he3hauka.toolvisuals.client.render.hitcolor;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.HIT_COLOR;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class HitColors {
    private HitColors() {}

    public static boolean active(ClientConfig config, LivingEntityRenderState state) {
        return state.hasRedOverlay && config.active(HIT_COLOR);
    }

    public static boolean armorActive(ClientConfig config, Object state) {
        return state instanceof LivingEntityRenderState living && active(config, living) && config.value(HIT_APPLY_TO) == 1;
    }

    public static int modelTint(ClientConfig config) {
        return UiPalette.mix(0xffffffff, 0xff000000 | config.color(HIT_TINT), config.number(HIT_STRENGTH));
    }

    public static int armorColor(int base, int hit, float strength) {
        int alpha = Math.round(strength * 255);
        int color = base == 0 ? hit : UiPalette.mix(base, 0xff000000 | hit, strength);
        return Math.max(base >>> 24, alpha) << 24 | color & 0xffffff;
    }
}

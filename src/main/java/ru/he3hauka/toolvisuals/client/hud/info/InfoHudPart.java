package ru.he3hauka.toolvisuals.client.hud.info;

import java.util.List;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public enum InfoHudPart {
    BRAND(HudElement.INFO_BRAND, null, HUD_SEPARATE_BRAND, UiPaint.Icon.RENDER, 4),
    PING(HudElement.INFO_PING, HUD_PING, HUD_SEPARATE_PING, UiPaint.Icon.PING, 2),
    FPS(HudElement.INFO_FPS, HUD_FPS, HUD_SEPARATE_FPS, UiPaint.Icon.FPS, 1),
    BPS(HudElement.INFO_BPS, HUD_BPS, HUD_SEPARATE_BPS, UiPaint.Icon.ARROW, 0),
    CLOCK(HudElement.INFO_CLOCK, HUD_CLOCK, HUD_SEPARATE_CLOCK, UiPaint.Icon.CLOCK, 3),
    COORDINATES(HudElement.INFO_COORDINATES, HUD_COORDINATES, HUD_SEPARATE_COORDINATES, null, 5);

    public static final List<InfoHudPart> ALL = List.of(values());
    public final HudElement element;
    public final VisualSetting visibility, separate;
    public final UiPaint.Icon icon;
    public final int defaultOrder;

    InfoHudPart(HudElement element, VisualSetting visibility, VisualSetting separate, UiPaint.Icon icon, int defaultOrder) {
        this.element = element;
        this.visibility = visibility;
        this.separate = separate;
        this.icon = icon;
        this.defaultOrder = defaultOrder;
    }

    public boolean enabled(ClientConfig config) { return visibility == null || config.flag(visibility); }

    public static InfoHudPart forElement(HudElement element) {
        for (var part : ALL) if (part.element == element) return part;
        return null;
    }

    public static boolean hasJoined(ClientConfig config) {
        for (var part : ALL) if (part.enabled(config) && !config.flag(part.separate)) return true;
        return false;
    }
}

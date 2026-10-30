package ru.he3hauka.toolvisuals.client.hud.layout.resize;

import java.util.Map;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import static java.util.Map.entry;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class HudScale {
    public static final double MIN=.5,MAX=3,STEP=.05;
    private static final Map<VisualModule,VisualSetting> CONTROLS=Map.ofEntries(
            entry(INFO_HUD,INFO_WHEEL_RESIZE),entry(ARMOR_HUD,ARMOR_WHEEL_RESIZE),entry(TARGET_HUD,TARGET_WHEEL_RESIZE),
            entry(HEALTH_INDICATOR,HEALTH_WHEEL_RESIZE),entry(KEYSTROKES,KEYS_WHEEL_RESIZE),entry(POTION_HUD,POTION_WHEEL_RESIZE),
            entry(INVENTORY_HUD,INVENTORY_WHEEL_RESIZE),entry(COOLDOWN_HUD,COOLDOWN_WHEEL_RESIZE),entry(FRIENDS_LIST,FRIENDS_WHEEL_RESIZE),
            entry(CLIENT_LIST,CLIENT_LIST_WHEEL_RESIZE),entry(SESSION_STATS,SESSION_WHEEL_RESIZE),entry(TOOL_PRESENTS,PRESENTS_WHEEL_RESIZE),
            entry(BOSS_BAR_CUSTOMIZER,BOSS_WHEEL_RESIZE),entry(MUSIC_HUD,MUSIC_WHEEL_RESIZE));
    private HudScale(){}
    public static double sanitize(double scale){return Double.isFinite(scale)?Math.round(Math.clamp(scale,MIN,MAX)*10000)/10000d:1;}
    public static boolean enabled(HudElement element,ClientConfig config){return config.flag(CONTROLS.get(element.module));}
    public static double next(double scale,double wheel){return sanitize(sanitize(scale)+Math.clamp(wheel,-10,10)*STEP);}
}

package ru.he3hauka.toolvisuals.client.hud.health;

import ru.he3hauka.toolvisuals.client.module.VisualSetting;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class HealthStyle {
    private HealthStyle() {}
    public static int number(float health){return Float.isFinite(health)?(int)Math.clamp(health,0,1024):0;}
    public static VisualSetting color(int health){
        return health<=5?HEALTH_INDICATOR_LOW_COLOR:health<=10?HEALTH_INDICATOR_HURT_COLOR:health<=15?HEALTH_INDICATOR_MEDIUM_COLOR:health<=20?HEALTH_INDICATOR_HIGH_COLOR:HEALTH_INDICATOR_OVERFLOW_COLOR;
    }
    public static float scale(int health,boolean enlarge,float normal,float critical){return health<10&&enlarge?critical:normal;}
}

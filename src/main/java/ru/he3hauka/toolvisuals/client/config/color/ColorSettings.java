package ru.he3hauka.toolvisuals.client.config.color;

import java.util.EnumSet;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ColorSettings {
    public static final int[] PRESETS = {0xb69aff,0xff87b7,0x77caff,0x7cebc5,0xffc776,0xf5f2ff};
    private static final EnumSet<VisualSetting> COLORS = EnumSet.of(BLOCK_HIGHLIGHT_COLOR,AMBIENT_COLOR,HIT_PARTICLE_COLOR,DEATH_TINT,CROSSHAIR_COLOR,CIRCLE_COLOR,TRAIL_COLOR,HIT_TINT,HITBOX_COLOR,HITBOX_EYE_COLOR,HITBOX_DIRECTION_COLOR,PREDICT_COLOR,TOTEM_COLOR,HAT_COLOR,TARGET_COLOR,BUBBLE_COLOR,SKY_TINT,ARMOR_COLOR_1,ARMOR_COLOR_2,ARMOR_COLOR_3,ARMOR_COLOR_4,ARMOR_COLOR_5,HEALTH_INDICATOR_HIGH_COLOR,HEALTH_INDICATOR_MEDIUM_COLOR,HEALTH_INDICATOR_LOW_COLOR,HEALTH_INDICATOR_HURT_COLOR,HEALTH_INDICATOR_OVERFLOW_COLOR);
    private ColorSettings() {}
    public static boolean contains(VisualSetting setting) { return COLORS.contains(setting) || setting.module==VisualModule.DAMAGE_VIGNETTE&&setting.name().startsWith("VIGNETTE_COLOR_"); }
    public static int legacy(ClientConfig config,VisualSetting setting) {
        if(setting.name().startsWith("ARMOR_COLOR_")||setting.module==VisualModule.HEALTH_INDICATOR||setting.module==VisualModule.DAMAGE_VIGNETTE)return (int)config.value(setting);
        if(setting==CROSSHAIR_COLOR&&config.value(setting)==6)return (int)config.value(CROSSHAIR_RED)<<16|(int)config.value(CROSSHAIR_GREEN)<<8|(int)config.value(CROSSHAIR_BLUE);
        int index=(int)config.value(setting);
        if(setting==SKY_TINT)index=new int[]{0,0,1,2,4}[Math.clamp(index,0,4)];
        return PRESETS[Math.clamp(index,0,PRESETS.length-1)];
    }
}

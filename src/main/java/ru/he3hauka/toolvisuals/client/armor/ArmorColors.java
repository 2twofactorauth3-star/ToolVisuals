package ru.he3hauka.toolvisuals.client.armor;

import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ArmorColors {
    public static final VisualSetting[] LEVELS = {ARMOR_LEVEL_1, ARMOR_LEVEL_2, ARMOR_LEVEL_3, ARMOR_LEVEL_4, ARMOR_LEVEL_5};
    public static final VisualSetting[] COLORS = {ARMOR_COLOR_1, ARMOR_COLOR_2, ARMOR_COLOR_3, ARMOR_COLOR_4, ARMOR_COLOR_5};
    private ArmorColors() {}
    public static int index(ClientConfig config, float percent) {
        int selected = -1; double limit = Double.POSITIVE_INFINITY;
        for (int i = 0; i < LEVELS.length; i++) {
            double threshold = config.value(LEVELS[i]);
            if (percent <= threshold && threshold <= limit) { limit = threshold; selected = i; }
        }
        return selected;
    }
    public static int color(ClientConfig config, float percent) {
        int index = index(config, percent);
        return index < 0 ? 0xff7cebc5 : 0xff000000 | config.color(COLORS[index]);
    }
}

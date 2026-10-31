package ru.he3hauka.toolvisuals.client.render.weather;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

public final class PrecipitationVisibility {
    private PrecipitationVisibility() {
    }

    public static boolean hides(ClientConfig config, RenderOption option) {
        return (option == RenderOption.RAIN || option == RenderOption.SNOW)
                && config.active(VisualModule.OPTIMIZATION)
                && config.flag(VisualSetting.OPT_HIDE_PRECIPITATION)
                && config.available(option);
    }
}

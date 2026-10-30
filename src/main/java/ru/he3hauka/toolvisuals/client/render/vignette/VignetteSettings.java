package ru.he3hauka.toolvisuals.client.render.vignette;

import java.util.List;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class VignetteSettings {
    public record Level(VisualSetting health, VisualSetting opacity, VisualSetting color) {}
    public static final List<Level> LEVELS=List.of(
            new Level(VIGNETTE_HP_1,VIGNETTE_OPACITY_1,VIGNETTE_COLOR_1),
            new Level(VIGNETTE_HP_2,VIGNETTE_OPACITY_2,VIGNETTE_COLOR_2),
            new Level(VIGNETTE_HP_3,VIGNETTE_OPACITY_3,VIGNETTE_COLOR_3));

    private VignetteSettings() {}

    public static VignetteCurve curve(ClientConfig config) {
        return new VignetteCurve(config.value(VIGNETTE_START_HP),LEVELS.stream()
                .map(level->new VignetteCurve.Band(config.value(level.health()),config.value(level.opacity()),config.colorStyle(level.color()))).toList());
    }
}

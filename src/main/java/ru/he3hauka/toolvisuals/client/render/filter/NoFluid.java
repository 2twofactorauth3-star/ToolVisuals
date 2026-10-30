package ru.he3hauka.toolvisuals.client.render.filter;

import net.minecraft.world.level.material.FogType;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.NO_FLUID;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class NoFluid {
    private NoFluid() {}
    public static boolean clears(FogType fluid) {
        var config = ToolVisualsClient.CONFIG;
        return config.active(NO_FLUID) && (fluid == FogType.WATER && config.flag(NO_FLUID_WATER)
                || fluid == FogType.LAVA && config.flag(NO_FLUID_LAVA));
    }
}

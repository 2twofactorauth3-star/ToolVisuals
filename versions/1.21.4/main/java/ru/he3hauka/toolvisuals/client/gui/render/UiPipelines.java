package ru.he3hauka.toolvisuals.client.gui.render;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import ru.he3hauka.toolvisuals.client.compatibility.render.RasterPipelines;

public final class UiPipelines {
    public static final Function<ResourceLocation,RenderType> TEXT=RasterPipelines::text,RASTER_TEXT=RasterPipelines::raster;
    private UiPipelines() {}
}

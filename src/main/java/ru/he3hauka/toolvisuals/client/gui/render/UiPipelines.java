package ru.he3hauka.toolvisuals.client.gui.render;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;

public final class UiPipelines {
    public static final RenderPipeline TEXT = RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$guiTextSnippet())
            .withLocation(Identifier.fromNamespaceAndPath("toolvisuals", "pipeline/ui_text"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("toolvisuals", "core/ui_text"))
            .build();
    public static final RenderPipeline RASTER_TEXT=RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$guiTexturedSnippet())
            .withLocation(Identifier.fromNamespaceAndPath("toolvisuals","pipeline/ui_raster_text"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("toolvisuals","core/ui_raster_text"))
            .build();

    private UiPipelines() {}
}

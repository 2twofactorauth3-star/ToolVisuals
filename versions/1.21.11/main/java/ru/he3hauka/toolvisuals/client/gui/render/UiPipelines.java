package ru.he3hauka.toolvisuals.client.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;

public final class UiPipelines {
    public static final RenderPipeline TEXT = RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$guiTextSnippet())
            .withLocation(Identifier.parse("toolvisuals:pipeline/ui_text"))
            .withVertexShader(RenderPipelines.GUI_TEXT.getVertexShader())
            .withFragmentShader(Identifier.parse("toolvisuals:core/ui_text"))
            .build();
    public static final RenderPipeline RASTER_TEXT = RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$guiTexturedSnippet())
            .withLocation(Identifier.parse("toolvisuals:pipeline/ui_raster_text"))
            .withVertexShader(RenderPipelines.GUI_TEXTURED.getVertexShader())
            .withFragmentShader(Identifier.parse("toolvisuals:core/ui_raster_text"))
            .build();

    private UiPipelines() {}
}

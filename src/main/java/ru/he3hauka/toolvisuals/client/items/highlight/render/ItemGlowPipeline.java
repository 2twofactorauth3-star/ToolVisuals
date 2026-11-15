package ru.he3hauka.toolvisuals.client.items.highlight.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;
import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;

public final class ItemGlowPipeline {
    public static final RenderPipeline GUI = RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$guiTexturedSnippet())
            .withLocation(Identifier.parse("toolvisuals:pipeline/item_glow"))
            .withVertexShader(Identifier.parse("toolvisuals:core/item_glow"))
            .withFragmentShader(Identifier.parse("toolvisuals:core/item_glow"))
            .withVertexBinding(0, DefaultVertexFormat.ENTITY)
            .build();
    private ItemGlowPipeline() {}
    public static void register() { RenderPipelinesAccessor.toolvisuals$register(GUI); }
}

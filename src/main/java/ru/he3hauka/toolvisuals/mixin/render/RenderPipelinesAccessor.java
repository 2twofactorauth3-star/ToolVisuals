package ru.he3hauka.toolvisuals.mixin.render;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("ENTITY_SNIPPET")
    static RenderPipeline.Snippet toolvisuals$entitySnippet() { throw new AssertionError(); }
    @Accessor("DEBUG_FILLED_SNIPPET")
    static RenderPipeline.Snippet toolvisuals$debugFilledSnippet() { throw new AssertionError(); }
    @Accessor("GUI_TEXT_SNIPPET")
    static RenderPipeline.Snippet toolvisuals$guiTextSnippet() { throw new AssertionError(); }
    @Accessor("GUI_TEXTURED_SNIPPET")
    static RenderPipeline.Snippet toolvisuals$guiTexturedSnippet() { throw new AssertionError(); }

    @Invoker("register")
    static RenderPipeline toolvisuals$register(RenderPipeline pipeline) { throw new AssertionError(); }
}

package ru.he3hauka.toolvisuals.mixin.render;
import java.util.Map;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderSetup.class)
public interface RenderSetupAccessor {
    @Accessor("oitPipelineSet") OitPipelineSet toolvisuals$oit();
    @Accessor("textures") Map<String, ?> toolvisuals$textures();
    @Accessor("useLightmap") boolean toolvisuals$lightmap();
    @Accessor("useOverlay") boolean toolvisuals$overlay();
    @Accessor("layeringTransform") LayeringTransform toolvisuals$layering();
    @Accessor("textureTransform") TextureTransform toolvisuals$transform();
    @Accessor("outlineProperty") RenderSetup.OutlineProperty toolvisuals$outline();
    @Accessor("outlineTextureName") String toolvisuals$outlineName();
    @Accessor("affectsCrumbling") boolean toolvisuals$crumbling();
    @Accessor("sortOnUpload") boolean toolvisuals$sort();
    @Accessor("forceSolidModelPhase") boolean toolvisuals$solid();
    @Invoker("<init>") static RenderSetup toolvisuals$create(RenderPipeline pipeline, OitPipelineSet oit,
            Map<String, ?> textures, boolean lightmap, boolean overlay, LayeringTransform layering,
            TextureTransform transform, RenderSetup.OutlineProperty outline, String outlineName,
            boolean crumbling, boolean sort, boolean solid) { throw new AssertionError(); }
}

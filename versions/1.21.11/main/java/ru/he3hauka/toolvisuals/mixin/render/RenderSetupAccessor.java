package ru.he3hauka.toolvisuals.mixin.render;

import java.util.Map;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderSetup.class)
public interface RenderSetupAccessor {
    @Accessor("textures") Map<String,?> toolvisuals$textures();
    @Accessor("useLightmap") boolean toolvisuals$lightmap();
    @Accessor("useOverlay") boolean toolvisuals$overlay();
    @Accessor("layeringTransform") LayeringTransform toolvisuals$layering();
    @Accessor("outputTarget") OutputTarget toolvisuals$output();
    @Accessor("textureTransform") TextureTransform toolvisuals$transform();
    @Accessor("outlineProperty") RenderSetup.OutlineProperty toolvisuals$outline();
    @Accessor("affectsCrumbling") boolean toolvisuals$crumbling();
    @Accessor("sortOnUpload") boolean toolvisuals$sort();
    @Accessor("bufferSize") int toolvisuals$bufferSize();
    @Invoker("<init>") static RenderSetup toolvisuals$create(RenderPipeline pipeline,Map<String,?> textures,boolean lightmap,boolean overlay,
            LayeringTransform layering,OutputTarget output,TextureTransform transform,RenderSetup.OutlineProperty outline,boolean crumbling,boolean sort,int size) {throw new AssertionError();}
}

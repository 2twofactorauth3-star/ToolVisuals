package ru.he3hauka.toolvisuals.client.compatibility.render;

import java.util.LinkedHashMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;

public final class RasterPipelines {
    private static final ShaderProgram TEXT=new ShaderProgram(ResourceLocation.parse("toolvisuals:core/ui_text"),DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,ShaderDefines.EMPTY);
    private static final ShaderProgram RASTER=new ShaderProgram(ResourceLocation.parse("toolvisuals:core/ui_raster_text"),DefaultVertexFormat.POSITION_TEX_COLOR,ShaderDefines.EMPTY);
    private static final LinkedHashMap<ResourceLocation,RenderType> TEXTURES=new LinkedHashMap<>(128,.75f,true),GLYPHS=new LinkedHashMap<>(32,.75f,true);
    private RasterPipelines() {}
    public static RenderType text(ResourceLocation texture){return type(GLYPHS,texture,RenderType.textIntensity(texture),TEXT);}
    public static RenderType raster(ResourceLocation texture){return type(TEXTURES,texture,RenderType.guiTextured(texture),RASTER);}
    private static RenderType type(LinkedHashMap<ResourceLocation,RenderType> cache,ResourceLocation texture,RenderType original,ShaderProgram shader){var type=cache.computeIfAbsent(texture,id->new LegacyRenderType("toolvisuals_raster_text",original,()->RenderSystem.setShader(shader)));if(cache.size()>512)cache.remove(cache.keySet().iterator().next());return type;}
}

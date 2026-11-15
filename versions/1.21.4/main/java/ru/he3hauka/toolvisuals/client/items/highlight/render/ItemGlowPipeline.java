package ru.he3hauka.toolvisuals.client.items.highlight.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import ru.he3hauka.toolvisuals.client.compatibility.render.LegacyRenderType;

public final class ItemGlowPipeline {
    private static final ShaderProgram PROGRAM = new ShaderProgram(ResourceLocation.parse("toolvisuals:core/item_glow"), DefaultVertexFormat.NEW_ENTITY, ShaderDefines.EMPTY);
    private static final RenderType BASE = RenderType.guiTextured(TextureAtlas.LOCATION_BLOCKS);
    private static int texture;
    private static final RenderType GUI = new LegacyRenderType("toolvisuals_item_glow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false,
            () -> { BASE.setupRenderState(); RenderSystem.setShader(PROGRAM); RenderSystem.setShaderTexture(0, texture); }, BASE::clearRenderState);
    private ItemGlowPipeline() {}
    public static void register() {}
    public static RenderType gui(int textureId) { texture = textureId; return GUI; }
}

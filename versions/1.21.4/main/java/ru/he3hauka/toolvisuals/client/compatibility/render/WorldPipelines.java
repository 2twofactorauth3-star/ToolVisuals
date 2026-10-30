package ru.he3hauka.toolvisuals.client.compatibility.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.CoreShaders;


public final class WorldPipelines extends RenderStateShard {
    public static final RenderType TRANSLUCENT=create("toolvisuals_world_effects",true),OPAQUE=create("toolvisuals_accessories",false);
    private WorldPipelines(){super("toolvisuals",()->{},()->{});}
    private static RenderType create(String name,boolean translucent){return new LegacyRenderType(name,DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,1536,translucent,()->{RenderSystem.setShader(CoreShaders.POSITION_COLOR);RenderSystem.enableDepthTest();RenderSystem.depthFunc(515);RenderSystem.depthMask(!translucent);RenderSystem.disableCull();if(translucent){RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();}else RenderSystem.disableBlend();},()->{RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.disableBlend();});}
    public static void initialize(){if(TRANSLUCENT==null||OPAQUE==null)throw new IllegalStateException("World render types were not initialized");}
}

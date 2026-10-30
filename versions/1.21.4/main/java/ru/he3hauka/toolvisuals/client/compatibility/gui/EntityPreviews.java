package ru.he3hauka.toolvisuals.client.compatibility.gui;

import java.util.IdentityHashMap;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class EntityPreviews {
    private static final IdentityHashMap<EntityRenderState,EntityRenderer<?,?>> RENDERERS=new IdentityHashMap<>();
    private EntityPreviews() {}
    public static void register(EntityRenderState state,EntityRenderer<?,?> renderer){if(RENDERERS.size()>=16)RENDERERS.clear();RENDERERS.put(state,renderer);}
    @SuppressWarnings({"rawtypes","unchecked"}) public static void draw(GuiGraphics graphics,EntityRenderState state,float scale,Vector3f center,Quaternionf rotation,Quaternionf camera,int left,int top,int right,int bottom){
        var client=Minecraft.getInstance();EntityRenderer renderer=RENDERERS.get(state);
        if(renderer==null&&state instanceof net.minecraft.client.renderer.entity.state.PlayerRenderState player)renderer=((ru.he3hauka.toolvisuals.mixin.render.PlayerRenderersAccessor)client.getEntityRenderDispatcher()).toolvisuals$playerRenderers().get(player.skin.model());
        if(renderer==null)return;
        graphics.flush();graphics.enableScissor(left,top,right,bottom);var pose=graphics.pose();pose.pushPose();var previousCamera=new Quaternionf(client.getEntityRenderDispatcher().cameraOrientation());float[] color=RenderSystem.getShaderColor().clone();
        try{float opacity=((ru.he3hauka.toolvisuals.client.hud.target.PortraitState)state).toolvisuals$portraitOpacity();RenderSystem.setShaderColor(1,1,1,Math.clamp(opacity,0,1));pose.setIdentity();pose.translate((left+right)/2f,(top+bottom)/2f,50);pose.scale(scale,scale,-scale);pose.mulPose(rotation);pose.translate(-center.x,-center.y,-center.z);Lighting.setupForEntityInInventory();client.getEntityRenderDispatcher().overrideCameraOrientation(new Quaternionf(camera).conjugate().rotateY((float)Math.PI));var buffers=client.renderBuffers().bufferSource();net.minecraft.client.renderer.MultiBufferSource translucent=type->buffers.getBuffer(ru.he3hauka.toolvisuals.client.compatibility.render.PortraitRenderTypes.transparent(type));renderer.render(state,pose,translucent,15728880);buffers.endBatch();}
        finally{RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);client.getEntityRenderDispatcher().overrideCameraOrientation(previousCamera);Lighting.setupFor3DItems();pose.popPose();graphics.disableScissor();}
    }
}

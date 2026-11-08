package ru.he3hauka.toolvisuals.client.compatibility.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import ru.he3hauka.toolvisuals.client.animation.death.DeathAscension;
import ru.he3hauka.toolvisuals.client.world.VisualWorld;

public final class LegacyWorld {
    private LegacyWorld() {}
    public static void initialize(){
        WorldRenderEvents.BEFORE_ENTITIES.register(context->{VersionCamera.capture(context.positionMatrix(),context.projectionMatrix(),context.frustum(),context.tickCounter().getGameTimeDeltaPartialTick(false));VisualWorld.extract(VersionCamera.SCENE);DeathAscension.extract(VersionCamera.SCENE);});
        WorldRenderEvents.AFTER_ENTITIES.register(context->{
            var client=Minecraft.getInstance();var camera=VersionCamera.SCENE.cameraRenderState.pos;var buffers=context.consumers();
            if(buffers==null)buffers=client.renderBuffers().bufferSource();
            for(var state:VersionCamera.SCENE.entityRenderStates){var pose=new PoseStack();pose.translate(state.x-camera.x,state.y-camera.y,state.z-camera.z);renderSoul(client,state,pose,buffers);}
        });
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context->{var buffers=context.consumers();if(buffers==null)buffers=Minecraft.getInstance().renderBuffers().bufferSource();try(var collector=new GeometryCollector(buffers)){VisualWorld.submit(VersionCamera.SCENE,collector);}});
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context,hit)->!ru.he3hauka.toolvisuals.client.render.blockoverlay.BlockOverlay.active()&&!ru.he3hauka.toolvisuals.client.render.filter.NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.BLOCK_OUTLINE));
    }
    @SuppressWarnings({"rawtypes","unchecked"}) private static void renderSoul(Minecraft client,EntityRenderState state,PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers){var player=(net.minecraft.client.renderer.entity.state.PlayerRenderState)state;EntityRenderer renderer=((ru.he3hauka.toolvisuals.mixin.render.PlayerRenderersAccessor)client.getEntityRenderDispatcher()).toolvisuals$playerRenderers().get(player.skin.model());if(renderer!=null)renderer.render(state,pose,buffers,15728880);}
}

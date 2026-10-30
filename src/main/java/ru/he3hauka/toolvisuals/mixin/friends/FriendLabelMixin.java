package ru.he3hauka.toolvisuals.mixin.friends;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.friends.FriendRenderState;
import ru.he3hauka.toolvisuals.client.render.label.NameTagRows;

@Mixin(EntityRenderer.class)
public abstract class FriendLabelMixin {
    @Inject(method="submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",at=@At("TAIL"))
    private void toolvisuals$friendLabel(EntityRenderState state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera,int offset,CallbackInfo ci) {
        if(!(state instanceof FriendRenderState friend)||friend.toolvisuals$friendLabel()==null||state.nameTag==null||state.nameTagAttachment==null)return;
        double height=NameTagRows.above(state.scoreText!=null,1);
        collector.submitNameTag(pose,state.nameTagAttachment.add(0,height,0),offset,friend.toolvisuals$friendLabel(),!state.isDiscrete,state.lightCoords,camera);
    }
}

package ru.he3hauka.toolvisuals.mixin.friends;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.friends.FriendRenderState;
import ru.he3hauka.toolvisuals.client.render.label.NameTagRows;

@Mixin(EntityRenderer.class)
public abstract class FriendLabelMixin {
    @Inject(method="render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("TAIL"))
    private void toolvisuals$friendLabel(EntityRenderState state,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci) {
        int offset=0;
        if(!(state instanceof FriendRenderState friend)||friend.toolvisuals$friendLabel()==null||state.nameTag==null||state.nameTagAttachment==null)return;
        double height=NameTagRows.above(ru.he3hauka.toolvisuals.client.compatibility.render.VersionState.hasScore(state),1);
        ru.he3hauka.toolvisuals.client.compatibility.render.NameLabels.draw(state,pose,buffers,light,height,friend.toolvisuals$friendLabel());
    }
}

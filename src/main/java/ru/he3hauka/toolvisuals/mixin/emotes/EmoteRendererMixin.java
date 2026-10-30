package ru.he3hauka.toolvisuals.mixin.emotes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.animation.*;

@Mixin(AvatarRenderer.class)
public abstract class EmoteRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"))
    private void toolvisuals$extract(Avatar player,AvatarRenderState state,float partial,CallbackInfo ci) {
        var visual=(EmoteVisualState)state;
        var frame=Emotes.pose(player.getUUID());
        visual.toolvisuals$emote(frame);visual.toolvisuals$offset(Vec3.ZERO);
        ru.he3hauka.toolvisuals.client.animation.movement.ExtraAnimations.extract((net.minecraft.world.entity.Entity)(Object)player,state,partial,frame!=null);
        if(frame==null)return;
        state.walkAnimationSpeed*=1-frame.weight;
        var stage=Emotes.staging(player.getUUID(),new Vec3(state.x,state.y,state.z),state.bodyRot);
        if(stage!=null){visual.toolvisuals$offset(stage.offset());state.bodyRot=stage.yaw();state.yRot*=1-stage.weight();state.xRot*=1-stage.weight();}
    }
    @Inject(method="getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$stage(AvatarRenderState state,CallbackInfoReturnable<Vec3> cir) {
        var offset=((EmoteVisualState)state).toolvisuals$offset();
        if(offset!=Vec3.ZERO)cir.setReturnValue(cir.getReturnValue().add(offset));
    }
    @Inject(method="setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",at=@At("TAIL"))
    private void toolvisuals$root(AvatarRenderState state,PoseStack pose,float body,float scale,CallbackInfo ci) {
        var frame=((EmoteVisualState)state).toolvisuals$emote();if(frame==null)return;
        pose.translate(0,frame.lift+.9,0);pose.rotate(Axis.XP,frame.root.x());pose.rotate(Axis.YP,frame.root.y());pose.rotate(Axis.ZP,frame.root.z());pose.translate(0,-.9,0);
    }
}

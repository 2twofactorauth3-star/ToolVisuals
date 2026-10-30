package ru.he3hauka.toolvisuals.mixin.emotes;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.animation.*;

@Mixin(HumanoidModel.class)
public abstract class EmoteModelMixin {
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",at=@At("TAIL"))
    private void toolvisuals$pose(HumanoidRenderState state,CallbackInfo ci) {
        ru.he3hauka.toolvisuals.client.animation.pose.PlayerPoseState.apply((HumanoidModel<?>)(Object)this,state);
        if(!(state instanceof EmoteVisualState visual)||visual.toolvisuals$emote()==null)return;
        var pose=visual.toolvisuals$emote();var model=(HumanoidModel<?>)(Object)this;
        toolvisuals$rotate(model.head,pose.head,pose.weight);toolvisuals$rotate(model.body,pose.body,pose.weight);
        toolvisuals$rotate(model.rightArm,pose.rightArm,pose.weight);toolvisuals$rotate(model.leftArm,pose.leftArm,pose.weight);
        toolvisuals$rotate(model.rightLeg,pose.rightLeg,pose.weight);toolvisuals$rotate(model.leftLeg,pose.leftLeg,pose.weight);
    }
    @Unique private static void toolvisuals$rotate(ModelPart part,EmotePose.Rotation rotation,float weight) {
        part.xRot+=(rotation.x()-part.xRot)*weight;part.yRot+=(rotation.y()-part.yRot)*weight;part.zRot+=(rotation.z()-part.zRot)*weight;
    }
}

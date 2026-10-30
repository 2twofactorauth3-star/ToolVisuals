package ru.he3hauka.toolvisuals.client.animation.pose;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public interface PlayerPoseState {
    PlayerPose toolvisuals$actionPose();
    void toolvisuals$actionPose(PlayerPose pose);
    static void apply(HumanoidModel<?> model,Object state){
        if(!(state instanceof PlayerPoseState visual)||visual.toolvisuals$actionPose()==null)return;
        var p=visual.toolvisuals$actionPose();apply(model.head,p.head());apply(model.body,p.body());apply(model.rightArm,p.rightArm());apply(model.leftArm,p.leftArm());apply(model.rightLeg,p.rightLeg());apply(model.leftLeg,p.leftLeg());
    }
    private static void apply(ModelPart part,PlayerPose.Limb limb){if(limb==null)return;float w=limb.weight();part.xRot+=(limb.x()-part.xRot)*w;part.yRot+=(limb.y()-part.yRot)*w;part.zRot+=(limb.z()-part.zRot)*w;}
    static void clear(Object state){if(state instanceof PlayerPoseState visual)visual.toolvisuals$actionPose(null);}
}

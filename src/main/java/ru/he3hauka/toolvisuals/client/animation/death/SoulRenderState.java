package ru.he3hauka.toolvisuals.client.animation.death;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;

import ru.he3hauka.toolvisuals.client.animation.pose.*;

import static ru.he3hauka.toolvisuals.client.animation.pose.PlayerPose.limb;

public final class SoulRenderState extends AvatarRenderState implements PlayerPoseState {
    public final int tint;
    private PlayerPose actionPose;
    public SoulRenderState(PlayerSkin skin,EntityType<?> type,float yaw,AscensionTimeline.Soul<?> soul,AscensionTimeline.Frame frame,int rgb,int light){
        this.skin=skin;bodyRot=yaw;entityType=type;id=soul.entity();
        x=soul.x();y=soul.y()+frame.rise();z=soul.z();scale=ageScale=1;pose=Pose.STANDING;
        boundingBoxWidth=.6f;boundingBoxHeight=1.8f;eyeHeight=1.62f;lightCoords=light;
        mainArm=HumanoidArm.RIGHT;leftArmPose=rightArmPose=HumanoidModel.ArmPose.EMPTY;
        rightHandItemStack=leftHandItemStack=headEquipment=chestEquipment=legsEquipment=feetEquipment=ItemStack.EMPTY;
        showHat=showJacket=showLeftPants=showRightPants=showLeftSleeve=showRightSleeve=true;
        tint=Math.clamp(Math.round(frame.alpha()*255),0,255)<<24|rgb&0xffffff;
        double spread=.12+AscensionTimeline.ease(frame.progress()/.18)*.48;
        actionPose=new PlayerPose(limb(-.17,0,0),limb(-.035,0,0),limb(-.12,0,spread),limb(-.12,0,-spread),limb(.035,0,.04),limb(.035,0,-.04));
    }
    @Override public PlayerPose toolvisuals$actionPose(){return actionPose;}
    @Override public void toolvisuals$actionPose(PlayerPose pose){actionPose=pose;}
}

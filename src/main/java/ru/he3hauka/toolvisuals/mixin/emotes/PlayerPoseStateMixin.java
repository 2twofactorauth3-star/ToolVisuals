package ru.he3hauka.toolvisuals.mixin.emotes;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.*;

import ru.he3hauka.toolvisuals.client.animation.pose.*;

@Mixin(AvatarRenderState.class)
public abstract class PlayerPoseStateMixin implements PlayerPoseState {
    @Unique private PlayerPose toolvisuals$actionPose;
    public PlayerPose toolvisuals$actionPose(){return toolvisuals$actionPose;}
    public void toolvisuals$actionPose(PlayerPose value){toolvisuals$actionPose=value;}
}

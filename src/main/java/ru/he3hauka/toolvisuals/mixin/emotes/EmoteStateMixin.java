package ru.he3hauka.toolvisuals.mixin.emotes;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.*;

import ru.he3hauka.toolvisuals.client.animation.*;

@Mixin(AvatarRenderState.class)
public abstract class EmoteStateMixin implements EmoteVisualState {
    @Unique private EmotePose toolvisuals$pose;
    @Unique private net.minecraft.world.phys.Vec3 toolvisuals$offset=net.minecraft.world.phys.Vec3.ZERO;
    public EmotePose toolvisuals$emote(){return toolvisuals$pose;}
    public void toolvisuals$emote(EmotePose value){toolvisuals$pose=value;}
    public net.minecraft.world.phys.Vec3 toolvisuals$offset(){return toolvisuals$offset;}
    public void toolvisuals$offset(net.minecraft.world.phys.Vec3 value){toolvisuals$offset=value;}
}

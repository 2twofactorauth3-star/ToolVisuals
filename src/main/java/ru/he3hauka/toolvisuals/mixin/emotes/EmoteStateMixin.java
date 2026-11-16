package ru.he3hauka.toolvisuals.mixin.emotes;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import ru.he3hauka.toolvisuals.client.animation.*;

@Mixin(AvatarRenderState.class)
public abstract class EmoteStateMixin implements EmoteVisualState {
    @Unique private EmotePose toolvisuals$pose;
    @Unique private Vec3 toolvisuals$offset=Vec3.ZERO;
    public EmotePose toolvisuals$emote(){return toolvisuals$pose;}
    public void toolvisuals$emote(EmotePose value){toolvisuals$pose=value;}
    public Vec3 toolvisuals$offset(){return toolvisuals$offset;}
    public void toolvisuals$offset(Vec3 value){toolvisuals$offset=value;}
}

package ru.he3hauka.toolvisuals.client.animation;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;
import ru.he3hauka.toolvisuals.client.animation.pose.PlayerPoseState;


public interface EmoteVisualState {
    EmotePose toolvisuals$emote();
    void toolvisuals$emote(EmotePose pose);
    Vec3 toolvisuals$offset();
    void toolvisuals$offset(Vec3 offset);
    static void clearPreview(EntityRenderState state) {
        PlayerPoseState.clear(state);
        if (state instanceof EmoteVisualState visual) {
            visual.toolvisuals$emote(null);
            visual.toolvisuals$offset(Vec3.ZERO);
        }
    }
}

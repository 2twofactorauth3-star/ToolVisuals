package ru.he3hauka.toolvisuals.client.animation;

public interface EmoteVisualState {
    EmotePose toolvisuals$emote();
    void toolvisuals$emote(EmotePose pose);
    net.minecraft.world.phys.Vec3 toolvisuals$offset();
    void toolvisuals$offset(net.minecraft.world.phys.Vec3 offset);
    static void clearPreview(net.minecraft.client.renderer.entity.state.EntityRenderState state) {
        ru.he3hauka.toolvisuals.client.animation.pose.PlayerPoseState.clear(state);
        if (state instanceof EmoteVisualState visual) {
            visual.toolvisuals$emote(null);
            visual.toolvisuals$offset(net.minecraft.world.phys.Vec3.ZERO);
        }
    }
}

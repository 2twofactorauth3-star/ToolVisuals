package ru.he3hauka.toolvisuals.client.compatibility.render;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class VersionState {
    private VersionState() {}
    public static boolean hasScore(Object state) {return state instanceof AvatarRenderState avatar&&avatar.scoreText!=null;}
    public static void clearScore(Object state) {if(state instanceof AvatarRenderState avatar)avatar.scoreText=null;}
}

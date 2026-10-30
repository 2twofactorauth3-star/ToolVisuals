package ru.he3hauka.toolvisuals.client.cape.render;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import ru.he3hauka.toolvisuals.client.cape.CapeClient;

public final class CapeRenderRules {
    private CapeRenderRules() {}
    public static boolean custom(AvatarRenderState state){return state.skin!=null&&state.skin.cape()!=null&&CapeClient.custom(state.skin.cape().texturePath());}
}

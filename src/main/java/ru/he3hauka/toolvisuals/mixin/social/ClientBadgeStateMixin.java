package ru.he3hauka.toolvisuals.mixin.social;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.he3hauka.toolvisuals.client.social.badge.BadgeRenderState;

@Mixin(AvatarRenderState.class)
public abstract class ClientBadgeStateMixin implements BadgeRenderState {
    @Unique private boolean toolvisuals$badgeAbove;

    public boolean toolvisuals$badgeAbove() { return toolvisuals$badgeAbove; }
    public void toolvisuals$badgeAbove(boolean value) { toolvisuals$badgeAbove = value; }
}

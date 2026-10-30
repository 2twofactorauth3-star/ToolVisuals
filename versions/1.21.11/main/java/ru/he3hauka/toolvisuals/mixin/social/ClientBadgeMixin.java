package ru.he3hauka.toolvisuals.mixin.social;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.social.SocialClient;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.friends.FriendRenderState;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.label.NameTagRows;
import ru.he3hauka.toolvisuals.client.social.badge.BadgeLabels;
import ru.he3hauka.toolvisuals.client.social.badge.BadgeRenderState;

@Mixin(EntityRenderer.class)
public abstract class ClientBadgeMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("RETURN"))
    private void toolvisuals$badge(Entity entity, EntityRenderState state, float partial, CallbackInfo ci) {
        if (!(state instanceof BadgeRenderState badge)) return;
        boolean visible = entity instanceof Player && state.nameTag != null && SocialClient.badgeVisible(entity.getUUID());
        var position = BadgeLabels.Position.at((int)ToolVisualsClient.CONFIG.value(VisualSetting.BADGE_POSITION));
        state.nameTag = BadgeLabels.apply(state.nameTag, visible, position, badge);
    }

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("TAIL"))
    private void toolvisuals$badgeAbove(EntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        int offset=0;
        if (!(state instanceof BadgeRenderState badge) || !badge.toolvisuals$badgeAbove()
                || state.nameTag == null || state.nameTagAttachment == null) return;
        boolean friendLabel = state instanceof FriendRenderState friend && friend.toolvisuals$friendLabel() != null;
        double height = NameTagRows.above(ru.he3hauka.toolvisuals.client.compatibility.render.VersionState.hasScore(state), friendLabel ? 2 : 1);
        collector.submitNameTag(pose, state.nameTagAttachment.add(0, height, 0), offset, BadgeLabels.ICON,
                !state.isDiscrete, state.lightCoords, state.distanceToCameraSq,camera);
    }
}

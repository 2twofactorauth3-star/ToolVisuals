package ru.he3hauka.toolvisuals.client.hud.target;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class TargetPortrait {
    private TargetPortrait() {}
    public static void draw(GuiGraphicsExtractor g, LivingEntity target, float x, float y, float size,float opacity) {
        boolean head = ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG.value(
                ru.he3hauka.toolvisuals.client.module.VisualSetting.TARGET_PORTRAIT) == 0;
        if (head && target instanceof net.minecraft.client.player.AbstractClientPlayer player) {
            net.minecraft.client.gui.components.PlayerFaceExtractor.extractRenderState(g, player.getSkin(),
                    Math.round(x), Math.round(y), Math.round(size),ru.he3hauka.toolvisuals.client.hud.target.PortraitTint.color(opacity,false));
            return;
        }
        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(target);
        var state = renderer.createRenderState(target, 1);
        ((ru.he3hauka.toolvisuals.client.hud.target.PortraitState)state).toolvisuals$portraitOpacity(opacity);
        ru.he3hauka.toolvisuals.client.animation.EmoteVisualState.clearPreview(state);
        state.nameTag = null; state.scoreText = null; state.displayFireAnimation = false;
        state.shadowPieces.clear(); state.leashStates = null; state.outlineColor = EntityRenderState.NO_OUTLINE; state.isInvisible = false;
        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 180; living.yRot = 0; living.xRot = 0;
            living.deathTime = 0; living.walkAnimationSpeed = 0; living.walkAnimationPos = 0;
            living.pose = Pose.STANDING; living.isInvisibleToPlayer = false;
        }
         
        var first = g.pose().transformPosition(x, y, new Vector2f());
        var last = g.pose().transformPosition(x + size, y + size, new Vector2f());
        float unitScale = (float)Math.hypot(g.pose().m00(), g.pose().m01());
        float modelScale = head ? size * 1.6f * unitScale / (target.isBaby() ? .7f : 1)
                : size * .9f * unitScale / Math.max(.2f, target.getBbHeight());
        g.entity(state, modelScale, new Vector3f(0, head ? target.getEyeHeight() - .08f : target.getBbHeight() / 2, 0),
                new Quaternionf().rotationZ((float)Math.PI), new Quaternionf(),
                Math.round(first.x), Math.round(first.y), Math.round(last.x), Math.round(last.y));
    }
}

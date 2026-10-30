package ru.he3hauka.toolvisuals.mixin.customhand;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.hand.HandSide;
import ru.he3hauka.toolvisuals.client.hand.render.HandRendering;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.CUSTOM_HAND;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class CustomHandMixin {
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "submitHandsWithItems",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;swingAnimation:F"))
    private float toolvisuals$firstPersonSwing(float original) {
        return ru.he3hauka.toolvisuals.client.render.NoSwing.firstPerson() ? 0 : original;
    }

    @WrapMethod(method="submitHandsWithItems")
    private void toolvisuals$handFrame(float partialTick,PoseStack pose,SubmitNodeCollector nodes,PlayerRenderState player,
            FirstPersonHandsAndItemsRenderState hands,Operation<Void> original) {
        HandRendering.beginFrame();
        try{original.call(partialTick,pose,nodes,player,hands);}finally{HandRendering.publish();}
    }

    @WrapMethod(method="submitArmWithItem")
    private void toolvisuals$individualHand(PlayerRenderState player,FirstPersonHandsAndItemsRenderState hands,float partial,
            float pitch,InteractionHand hand,float swing,ItemStack item,float equip,PoseStack pose,SubmitNodeCollector nodes,int light,Operation<Void> original) {
        var config=ToolVisualsClient.CONFIG;
        if(!config.active(CUSTOM_HAND)||player.avatarRenderState==null) {
            original.call(player,hands,partial,pitch,hand,swing,item,equip,pose,nodes,light);return;
        }
        var side=HandSide.of(hand,player.avatarRenderState.mainArm);var profile=HandRendering.PROFILES.pose(config,side);
        if(profile.hidden)return;
        pose.pushPose();var capture=HandRendering.begin(side,pose);
        try {
            profile.offset(pose);
            original.call(player,hands,partial,pitch,hand,profile.noSwing?0f:swing,item,profile.noEquip?0f:equip,pose,nodes,light);
        } finally { HandRendering.end(capture);pose.popPose(); }
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "submitArmWithItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void toolvisuals$heldItem(net.minecraft.client.renderer.item.ItemStackRenderState item, PoseStack pose,
            SubmitNodeCollector nodes, int light, int overlay, int outline,
            Operation<Void> original, @Local(argsOnly=true) PlayerRenderState player,
            @Local(argsOnly=true) InteractionHand hand) {
        var config = ToolVisualsClient.CONFIG;
        boolean custom = config.active(CUSTOM_HAND) && player.avatarRenderState != null;
        boolean mini = config.active(ru.he3hauka.toolvisuals.client.module.VisualModule.MINI_ITEMS) && config.flag(MINI_HAND);
        if (!custom && !mini) {
            original.call(item, pose, nodes, light, overlay, outline);
            return;
        }
        pose.pushPose();
        try {
            if (custom) {
                var side = HandSide.of(hand, player.avatarRenderState.mainArm);
                HandRendering.PROFILES.pose(config, side).shape(pose, item.getModelBoundingBox());
            }
            if (mini) {
                float scale = config.number(MINI_HAND_SCALE);
                pose.scale(scale, scale, scale);
            }
            HandRendering.item(pose,item);
            original.call(item, pose, nodes, light, overlay, outline);
        } finally { pose.popPose(); }
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$noSwing(float swing, PoseStack pose, int direction, HumanoidArm arm, CallbackInfo ci) {
        if (toolvisuals$noSwing(arm)) ci.cancel();
    }

    @Inject(method = "applyItemArmAttackTransform", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$noItemSwing(PoseStack pose, HumanoidArm arm, float swing, CallbackInfo ci) {
        if (toolvisuals$noSwing(arm)) ci.cancel();
    }

    private static boolean toolvisuals$noSwing(HumanoidArm arm) {
        var config=ToolVisualsClient.CONFIG;
        return config.active(CUSTOM_HAND)&&(config.flag(NO_SWING)||HandRendering.PROFILES.pose(config,HandSide.of(arm)).noSwing);
    }

    @WrapMethod(method="renderPlayerHand")
    private void toolvisuals$bareHand(PoseStack pose,SubmitNodeCollector nodes,int light,HumanoidArm arm,
            PlayerRenderState player,Operation<Void> original) {
        var config=ToolVisualsClient.CONFIG;
        if(!config.active(CUSTOM_HAND)) {
            original.call(pose,nodes,light,arm,player);
            return;
        }
        var profile=HandRendering.PROFILES.pose(config,HandSide.of(arm));
        if(profile.hidden)return;
        pose.pushPose();
        try {
            profile.shape(pose,HandRendering.armBounds(arm));
            HandRendering.arm(pose,arm);
            original.call(pose,nodes,light,arm,player);
        } finally {pose.popPose();}
    }

    @WrapMethod(method="renderMap")
    private void toolvisuals$heldMap(PoseStack pose,SubmitNodeCollector nodes,int light,ItemStack item,
            boolean mainHand,FirstPersonHandsAndItemsRenderState hands,Operation<Void> original) {
        var config=ToolVisualsClient.CONFIG;
        var player=net.minecraft.client.Minecraft.getInstance().player;
        if(!config.active(CUSTOM_HAND)||player==null) {
            original.call(pose,nodes,light,item,mainHand,hands);
            return;
        }
        var side=HandSide.of(mainHand?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND,player.getMainArm());
        pose.pushPose();
        try {
            HandRendering.PROFILES.pose(config,side).shape(pose,HandRendering.MAP_BOUNDS);
            HandRendering.map(pose);
            original.call(pose,nodes,light,item,mainHand,hands);
        } finally {pose.popPose();}
    }
}

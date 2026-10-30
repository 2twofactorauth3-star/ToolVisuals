package ru.he3hauka.toolvisuals.mixin.customhand;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.hand.HandSide;
import ru.he3hauka.toolvisuals.client.hand.render.HandRendering;
import ru.he3hauka.toolvisuals.client.render.NoSwing;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

@Mixin(ItemInHandRenderer.class)
public abstract class CustomHandMixin {
    @Unique private HandSide toolvisuals$side;
    @WrapMethod(method="renderHandsWithItems")
    private void toolvisuals$frame(float partial,PoseStack pose,SubmitNodeCollector nodes,LocalPlayer player,int light,Operation<Void> original) {
        HandRendering.beginFrame();try{original.call(partial,pose,nodes,player,light);}finally{HandRendering.publish();}
    }
    @WrapMethod(method="renderArmWithItem")
    private void toolvisuals$hand(AbstractClientPlayer player,float partial,float pitch,InteractionHand hand,float swing,ItemStack item,
            float equip,PoseStack pose,SubmitNodeCollector nodes,int light,Operation<Void> original) {
        var config=ToolVisualsClient.CONFIG;
        if(NoSwing.firstPerson())swing=0;
        if(!config.active(CUSTOM_HAND)){original.call(player,partial,pitch,hand,swing,item,equip,pose,nodes,light);return;}
        var side=HandSide.of(hand,player.getMainArm());var profile=HandRendering.PROFILES.pose(config,side);if(profile.hidden)return;
        var previous=toolvisuals$side;toolvisuals$side=side;pose.pushPose();var scope=HandRendering.begin(side,pose);
        try{profile.offset(pose);original.call(player,partial,pitch,hand,profile.noSwing?0:swing,item,profile.noEquip?0:equip,pose,nodes,light);}
        finally{HandRendering.end(scope);pose.popPose();toolvisuals$side=previous;}
    }
    @WrapOperation(method="renderItem",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void toolvisuals$item(ItemStackRenderState item,PoseStack pose,SubmitNodeCollector nodes,int light,int overlay,int outline,Operation<Void> original,
            @Local(argsOnly=true) ItemDisplayContext context) {
        boolean first=context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        var config=ToolVisualsClient.CONFIG;boolean custom=first&&config.active(CUSTOM_HAND),mini=first&&config.active(MINI_ITEMS)&&config.flag(MINI_HAND);
        if(!custom&&!mini){original.call(item,pose,nodes,light,overlay,outline);return;}
        pose.pushPose();
        try {
            if(custom)HandRendering.PROFILES.pose(config,context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND?HandSide.LEFT:HandSide.RIGHT).shape(pose,item.getModelBoundingBox());
            if(mini){float size=config.number(MINI_HAND_SCALE);pose.scale(size,size,size);}
            HandRendering.item(pose,item);original.call(item,pose,nodes,light,overlay,outline);
        }finally{pose.popPose();}
    }
    @WrapOperation(method={"renderPlayerArm","renderMapHand"},at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void toolvisuals$rightArm(AvatarRenderer renderer,PoseStack pose,SubmitNodeCollector nodes,int light,Identifier skin,boolean sleeve,Operation<Void> original) {
        toolvisuals$arm(renderer,pose,nodes,light,skin,sleeve,HumanoidArm.RIGHT,original);
    }
    @WrapOperation(method={"renderPlayerArm","renderMapHand"},at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void toolvisuals$leftArm(AvatarRenderer renderer,PoseStack pose,SubmitNodeCollector nodes,int light,Identifier skin,boolean sleeve,Operation<Void> original) {
        toolvisuals$arm(renderer,pose,nodes,light,skin,sleeve,HumanoidArm.LEFT,original);
    }
    @Unique private void toolvisuals$arm(AvatarRenderer renderer,PoseStack pose,SubmitNodeCollector nodes,int light,Identifier skin,boolean sleeve,HumanoidArm arm,Operation<Void> original) {
        if(!ToolVisualsClient.CONFIG.active(CUSTOM_HAND)){original.call(renderer,pose,nodes,light,skin,sleeve);return;}
        var profile=HandRendering.PROFILES.pose(ToolVisualsClient.CONFIG,HandSide.of(arm));if(profile.hidden)return;
        pose.pushPose();try{profile.shape(pose,HandRendering.armBounds(arm));HandRendering.arm(pose,arm);original.call(renderer,pose,nodes,light,skin,sleeve);}finally{pose.popPose();}
    }
    @WrapMethod(method="renderMap")
    private void toolvisuals$map(PoseStack pose,SubmitNodeCollector nodes,int light,ItemStack item,Operation<Void> original) {
        if(!ToolVisualsClient.CONFIG.active(CUSTOM_HAND)||toolvisuals$side==null){original.call(pose,nodes,light,item);return;}
        pose.pushPose();try{HandRendering.PROFILES.pose(ToolVisualsClient.CONFIG,toolvisuals$side).shape(pose,HandRendering.MAP_BOUNDS);HandRendering.map(pose);original.call(pose,nodes,light,item);}finally{pose.popPose();}
    }
}

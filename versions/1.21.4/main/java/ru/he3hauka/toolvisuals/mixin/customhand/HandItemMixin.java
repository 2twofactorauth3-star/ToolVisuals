package ru.he3hauka.toolvisuals.mixin.customhand;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.hand.HandSide;
import ru.he3hauka.toolvisuals.client.hand.render.HandRendering;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

@Mixin(ItemRenderer.class)
public abstract class HandItemMixin {
    @WrapOperation(method="renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/item/ItemStackRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void toolvisuals$item(ItemStackRenderState item,PoseStack pose,MultiBufferSource nodes,int light,int overlay,Operation<Void> original,
            @Local(argsOnly=true) ItemDisplayContext context) {
        boolean first=context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        var config=ToolVisualsClient.CONFIG;boolean custom=first&&config.active(CUSTOM_HAND),mini=first&&config.active(MINI_ITEMS)&&config.flag(MINI_HAND);
        if(!custom&&!mini){original.call(item,pose,nodes,light,overlay);return;}
        pose.pushPose();
        try {
            if(custom)HandRendering.PROFILES.pose(config,context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND?HandSide.LEFT:HandSide.RIGHT).shape(pose,item.getModelBoundingBox());
            if(mini){float size=config.number(MINI_HAND_SCALE);pose.scale(size,size,size);}
            HandRendering.item(pose,item);original.call(item,pose,nodes,light,overlay);
        }finally{pose.popPose();}
    }
}

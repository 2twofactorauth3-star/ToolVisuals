package ru.he3hauka.toolvisuals.mixin.items;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.items.ItemPhysic;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemPhysicMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",at=@At("TAIL"))
    private void toolvisuals$itemPhysics(ItemEntity entity,ItemEntityRenderState state,float partial,CallbackInfo ci){ItemPhysic.extract(entity,state,partial);}
    @WrapOperation(method="submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",ordinal=0))
    private void toolvisuals$itemHeight(PoseStack pose,float x,float y,float z,Operation<Void> original,
            ItemEntityRenderState state,PoseStack unused,SubmitNodeCollector nodes,CameraRenderState camera) {
        var data=(ItemPhysic.State)state;
        if(!data.toolvisuals$physical()){original.call(pose,x,y,z);return;}
        if(!data.toolvisuals$ground()){original.call(pose,x,.1f,z);return;}
        var box=state.item.getModelBoundingBox();
        if(box.getZsize()<=.0625)box=box.inflate(0,0,Math.max(0,state.count-1)*box.getZsize()*.75);
        original.call(pose,x,ItemPhysic.floorOffset(box,data.toolvisuals$pitch()),z);
    }
    @WrapOperation(method="submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/PoseStack;rotate(Lcom/mojang/math/Axis;F)V",ordinal=0))
    private void toolvisuals$itemRotation(PoseStack pose,Axis axis,float angle,Operation<Void> original,
            ItemEntityRenderState state,PoseStack unused,SubmitNodeCollector nodes,CameraRenderState camera) {
        var data=(ItemPhysic.State)state;
        if(!data.toolvisuals$physical()){original.call(pose,axis,angle);return;}
        original.call(pose,axis,(float)Math.toRadians(data.toolvisuals$yaw()));pose.rotateDegrees(Axis.XP,data.toolvisuals$pitch());
    }
}

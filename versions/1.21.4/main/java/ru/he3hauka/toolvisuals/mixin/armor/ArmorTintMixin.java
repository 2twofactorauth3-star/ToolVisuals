package ru.he3hauka.toolvisuals.mixin.armor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.armor.ArmorTint;
import ru.he3hauka.toolvisuals.client.armor.render.ArmorRenderContext;
import ru.he3hauka.toolvisuals.client.armor.render.ArmorRenderTypes;

@Mixin(EquipmentLayerRenderer.class)
public abstract class ArmorTintMixin {
    @WrapOperation(method="renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/ItemRenderer;getArmorFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;Z)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer toolvisuals$type(MultiBufferSource buffers,RenderType type,boolean foil,Operation<VertexConsumer> original,@Local(argsOnly=true) ItemStack stack){var state=ArmorRenderContext.current();if(ArmorTint.color(state,stack)!=0)type=ArmorRenderTypes.renderType(type,ArmorTint.filled(state));return original.call(buffers,type,foil);}
    @WrapOperation(method="renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void toolvisuals$tint(Model model,PoseStack pose,VertexConsumer vertices,int light,int overlay,int color,Operation<Void> original,@Local(argsOnly=true) ItemStack stack){int tint=ArmorTint.color(ArmorRenderContext.current(),stack);original.call(model,pose,vertices,light,overlay,tint==0?color:tint);}
    @WrapOperation(method="renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void toolvisuals$trim(Model model,PoseStack pose,VertexConsumer vertices,int light,int overlay,Operation<Void> original){var state=ArmorRenderContext.current();if(ArmorTint.filled(state))return;if(ArmorTint.hit(state))model.renderToBuffer(pose,vertices,light,overlay,ru.he3hauka.toolvisuals.client.render.hitcolor.HitColors.modelTint(ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG));else original.call(model,pose,vertices,light,overlay);}
}

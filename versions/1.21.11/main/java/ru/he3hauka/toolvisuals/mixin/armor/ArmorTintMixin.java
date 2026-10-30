package ru.he3hauka.toolvisuals.mixin.armor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.armor.ArmorTint;
import ru.he3hauka.toolvisuals.client.armor.render.ArmorRenderTypes;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.render.hitcolor.HitColors;

@Mixin(EquipmentLayerRenderer.class)
public abstract class ArmorTintMixin {
    @WrapOperation(method="renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"),require=3)
    private void toolvisuals$color(OrderedSubmitNodeCollector collector,Model<?> model,Object state,PoseStack pose,RenderType type,int light,int overlay,int color,
            TextureAtlasSprite trim,int outline,ModelFeatureRenderer.CrumblingOverlay crumbling,Operation<Void> original,@Local(argsOnly=true) ItemStack stack) {
        boolean fill=ArmorTint.filled(state);if(fill&&trim!=null)return;
        int tint=ArmorTint.color(state,stack);
        if(tint!=0&&trim==null){type=ArmorRenderTypes.renderType(type,fill);color=tint;}
        if(trim!=null&&ArmorTint.hit(state))color=net.minecraft.util.ARGB.multiply(color,HitColors.modelTint(ToolVisualsClient.CONFIG));
        original.call(collector,model,state,pose,type,light,overlay,color,trim,outline,crumbling);
    }
}

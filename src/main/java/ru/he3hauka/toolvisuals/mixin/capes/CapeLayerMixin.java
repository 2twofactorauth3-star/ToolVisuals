package ru.he3hauka.toolvisuals.mixin.capes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.cape.CapeClient;
import ru.he3hauka.toolvisuals.client.cape.render.CapeRenderRules;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryVisualState;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Inject(method="submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$backAccessory(PoseStack pose,SubmitNodeCollector collector,int light,AvatarRenderState state,float yaw,float pitch,CallbackInfo ci){
        var frame=((AccessoryVisualState)state).toolvisuals$accessories();if(frame!=null&&frame.hideCape&&!(CapeRenderRules.custom(state)&&ToolVisualsClient.CONFIG.flag(VisualSetting.CAPE_WITH_COSMETICS)))ci.cancel();
    }
    @WrapOperation(method="submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/layers/CapeLayer;hasLayer(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;)Z"))
    private boolean toolvisuals$withElytra(CapeLayer layer,ItemStack stack,EquipmentClientInfo.LayerType type,Operation<Boolean> original,PoseStack pose,SubmitNodeCollector collector,int light,AvatarRenderState state,float yaw,float pitch){
        return !(type==EquipmentClientInfo.LayerType.WINGS&&CapeRenderRules.custom(state)&&ToolVisualsClient.CONFIG.flag(VisualSetting.CAPE_WITH_ELYTRA))&&original.call(layer,stack,type);
    }
    @Redirect(method="submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/rendertype/RenderTypes;entitySolid(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType toolvisuals$transparency(Identifier texture){return CapeClient.custom(texture)?RenderTypes.entityTranslucent(texture):RenderTypes.entitySolid(texture);}
}

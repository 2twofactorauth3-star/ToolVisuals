package com.viaversion.viafabricplus.visuals.injection.mixin.petrified_oak_slab_model;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemModelManager.class)
public abstract class MixinItemRenderer {

    @Unique
    private static final Identifier viaFabricPlusVisuals$missingIdentifier = Identifier.of(String.valueOf(System.currentTimeMillis()));

    @Redirect(method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
    private Object removeModel(ItemStack instance, ComponentType componentType) {
        if (VisualSettings.INSTANCE.replacePetrifiedOakSlab.isEnabled() && instance.isOf(Items.PETRIFIED_OAK_SLAB)) {
            return viaFabricPlusVisuals$missingIdentifier;
        } else {
            return instance.get(componentType);
        }
    }

}

package com.viaversion.viafabricplus.visuals.injection.mixin.petrified_oak_slab_model;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemModelResolver.class)
public abstract class MixinItemRenderer {

    @Unique
    private static final Identifier viaFabricPlusVisuals$missingIdentifier = Identifier.parse(String.valueOf(System.currentTimeMillis()));

    @Redirect(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object removeModel(ItemStack instance, DataComponentType<?> componentType) {
        if (VisualSettings.INSTANCE.replacePetrifiedOakSlab.isEnabled() && instance.is(Items.PETRIFIED_OAK_SLAB)) {
            return viaFabricPlusVisuals$missingIdentifier;
        } else {
            return instance.get(componentType);
        }
    }

}

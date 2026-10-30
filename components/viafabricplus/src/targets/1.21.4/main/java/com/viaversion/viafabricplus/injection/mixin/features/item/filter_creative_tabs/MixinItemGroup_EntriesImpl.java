package com.viaversion.viafabricplus.injection.mixin.features.item.filter_creative_tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.features.item.filter_creative_tabs.ItemRegistryDiff;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.item.ItemGroup$EntriesImpl")
public abstract class MixinItemGroup_EntriesImpl {

    @Shadow
    @Final
    private ItemGroup group;

    @WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"))
    private boolean removeUnknownItems(Item instance, FeatureSet featureSet, Operation<Boolean> original) {
        final boolean originalValue = original.call(instance, featureSet);
        final int index = GeneralSettings.INSTANCE.removeNotAvailableItemsFromCreativeTab.getIndex();

        if (index == 2   || MinecraftClient.getInstance().isInSingleplayer()) {
            return originalValue;
        } else if (index == 1   && !Registries.ITEM_GROUP.getId(this.group).getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return originalValue;
        } else {
            return ItemRegistryDiff.keepItem(instance) && originalValue;
        }
    }

}

package com.viaversion.viafabricplus.injection.mixin.features.v26_2.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CampfireBlock.class)
public abstract class MixinCampfireBlock {

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean dontDouseInBlock(ItemStack instance, TagKey<Item> tag, Operation<Boolean> original) {

        return ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3) && original.call(instance, tag);
    }

}

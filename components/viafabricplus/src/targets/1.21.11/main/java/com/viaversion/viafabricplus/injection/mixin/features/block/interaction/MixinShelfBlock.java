package com.viaversion.viafabricplus.injection.mixin.features.block.interaction;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShelfBlock;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShelfBlock.class)
public abstract class MixinShelfBlock {

    @Redirect(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0))
    private boolean swingHand(ItemStack instance) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_11) && instance.isEmpty();
    }

}

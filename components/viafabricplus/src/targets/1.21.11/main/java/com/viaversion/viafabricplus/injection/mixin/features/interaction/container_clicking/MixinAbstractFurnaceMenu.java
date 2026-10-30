package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractFurnaceMenu.class)
public abstract class MixinAbstractFurnaceMenu {

    @Shadow
    protected abstract boolean canSmelt(ItemStack itemStack);

    @Shadow
    protected abstract boolean isFuel(ItemStack itemStack);

    @Redirect(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractFurnaceMenu;canSmelt(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean disableShiftClickSmeltingSlot(AbstractFurnaceMenu instance, ItemStack itemStack) {
        return this.canSmelt(itemStack) && ProtocolTranslator.getTargetVersion().newerThan(LegacyProtocolVersion.r1_2_1tor1_2_3);
    }

    @Redirect(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractFurnaceMenu;isFuel(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean disableShiftClickFuelSlot(AbstractFurnaceMenu instance, ItemStack itemStack) {
        return this.isFuel(itemStack) && ProtocolTranslator.getTargetVersion().newerThan(LegacyProtocolVersion.r1_2_1tor1_2_3);
    }

}

package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class MixinAbstractFurnaceScreenHandler {

    @Shadow
    protected abstract boolean isSmeltable(ItemStack itemStack);

    @Shadow
    protected abstract boolean isFuel(ItemStack itemStack);

    @Redirect(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/AbstractFurnaceScreenHandler;isSmeltable(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean disableShiftClickSmeltingSlot(AbstractFurnaceScreenHandler instance, ItemStack itemStack) {
        return this.isSmeltable(itemStack) && ProtocolTranslator.getTargetVersion().newerThan(LegacyProtocolVersion.r1_2_1tor1_2_3);
    }

    @Redirect(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/AbstractFurnaceScreenHandler;isFuel(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean disableShiftClickFuelSlot(AbstractFurnaceScreenHandler instance, ItemStack itemStack) {
        return this.isFuel(itemStack) && ProtocolTranslator.getTargetVersion().newerThan(LegacyProtocolVersion.r1_2_1tor1_2_3);
    }

}

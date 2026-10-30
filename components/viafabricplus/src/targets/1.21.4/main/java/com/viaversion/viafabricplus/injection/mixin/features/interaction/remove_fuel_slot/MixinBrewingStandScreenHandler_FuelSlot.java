package com.viaversion.viafabricplus.injection.mixin.features.interaction.remove_fuel_slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.BrewingStandScreenHandler$FuelSlot")
public abstract class MixinBrewingStandScreenHandler_FuelSlot extends Slot {

    public MixinBrewingStandScreenHandler_FuelSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "matches(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void removeFuelSlot(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean isEnabled() {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_8);
    }

}

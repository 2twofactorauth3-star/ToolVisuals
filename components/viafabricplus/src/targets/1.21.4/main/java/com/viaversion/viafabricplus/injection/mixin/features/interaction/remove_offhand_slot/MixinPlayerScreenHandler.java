package com.viaversion.viafabricplus.injection.mixin.features.interaction.remove_offhand_slot;

import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(PlayerScreenHandler.class)
public abstract class MixinPlayerScreenHandler extends AbstractCraftingScreenHandler {

    public MixinPlayerScreenHandler(ScreenHandlerType<?> type, int syncId, int width, int height) {
        super(type, syncId, width, height);
    }

    @Redirect(method = "<init>",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler$1;<init>(Lnet/minecraft/screen/PlayerScreenHandler;Lnet/minecraft/inventory/Inventory;IIILnet/minecraft/entity/player/PlayerEntity;)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 0))
    private Slot removeOffhandSlot(PlayerScreenHandler screenHandler, Slot slot) {
        return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8) ? null : addSlot(slot);
    }

}

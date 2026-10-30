package com.viaversion.viafabricplus.injection.mixin.features.interaction.container_clicking;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingScreenHandler.class)
public abstract class MixinCraftingScreenHandler extends AbstractCraftingScreenHandler {

    public MixinCraftingScreenHandler(ScreenHandlerType<?> type, int syncId, int width, int height) {
        super(type, syncId, width, height);
    }

    @Redirect(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/CraftingScreenHandler;insertItem(Lnet/minecraft/item/ItemStack;IIZ)Z", ordinal = 1))
    private boolean noShiftClickMoveIntoCraftingTable(CraftingScreenHandler instance, ItemStack itemStack, int startIndex, int endIndex, boolean fromLast) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_14_4) && this.insertItem(itemStack, startIndex, endIndex, fromLast);
    }

}

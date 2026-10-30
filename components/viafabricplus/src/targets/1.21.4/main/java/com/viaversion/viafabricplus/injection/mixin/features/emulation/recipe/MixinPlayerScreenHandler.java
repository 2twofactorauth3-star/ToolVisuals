package com.viaversion.viafabricplus.injection.mixin.features.emulation.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import com.viaversion.viafabricplus.features.emulation.recipe.Recipes1_11_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public abstract class MixinPlayerScreenHandler extends AbstractCraftingScreenHandler {

    public MixinPlayerScreenHandler(ScreenHandlerType<?> type, int syncId, int width, int height) {
        super(type, syncId, width, height);
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    private void clientSideCrafting(Inventory inventory, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_11_1)) {
            Recipes1_11_2.setCraftingResultSlot(syncId, this, this.craftingInventory);
        }
    }

}

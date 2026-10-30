package com.viaversion.viafabricplus.injection.mixin.features.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;

import com.viaversion.viafabricplus.features.recipe.Recipes1_11_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class MixinCraftingMenu extends AbstractCraftingMenu {

    public MixinCraftingMenu(MenuType<?> type, int syncId, int width, int height) {
        super(type, syncId, width, height);
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void clientSideCrafting(Container inventory, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_11_1)) {
            Recipes1_11_2.setCraftingResultSlot(containerId, this, this.craftSlots);
        }
    }

}

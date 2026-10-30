package com.viaversion.viafabricplus.visuals.injection.mixin.classic.creative_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import com.viaversion.viafabricplus.visuals.features.classic.creative_menu.GridItemSelectionScreen;
import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen {

    @Inject(method = "init", at = @At("RETURN"))
    private void replaceCreativeMenu(CallbackInfo ci) {
        if (VisualSettings.INSTANCE.replaceCreativeInventory.isEnabled()) {
            Minecraft.getInstance().setScreen(GridItemSelectionScreen.INSTANCE);
        }
    }

}

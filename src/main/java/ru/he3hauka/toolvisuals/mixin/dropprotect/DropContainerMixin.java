package ru.he3hauka.toolvisuals.mixin.dropprotect;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.items.dropprotect.DropProtect;

@Mixin({AbstractContainerScreen.class, CreativeModeInventoryScreen.class})
public abstract class DropContainerMixin {
    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$protectDrop(Slot slot, int index, int button, ContainerInput action, CallbackInfo ci) {
        if (DropProtect.inventory((Screen)(Object)this, slot, index, button, action)) ci.cancel();
    }
}

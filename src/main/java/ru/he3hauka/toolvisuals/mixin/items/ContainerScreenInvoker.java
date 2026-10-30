package ru.he3hauka.toolvisuals.mixin.items;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenInvoker {
    @Invoker("slotClicked")
    void toolvisuals$slotClicked(Slot slot,int index,int button,ContainerInput input);
}

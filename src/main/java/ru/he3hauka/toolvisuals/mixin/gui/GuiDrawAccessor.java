package ru.he3hauka.toolvisuals.mixin.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.render.GuiRenderer$Draw")
public interface GuiDrawAccessor {
    @Accessor("scissorArea") ScreenRectangle toolvisuals$scissorArea();
}

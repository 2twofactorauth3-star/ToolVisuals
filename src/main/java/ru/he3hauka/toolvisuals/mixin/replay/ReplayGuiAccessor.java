package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface ReplayGuiAccessor {
    @Accessor("screen") void toolvisuals$replayScreen(Screen screen);
}

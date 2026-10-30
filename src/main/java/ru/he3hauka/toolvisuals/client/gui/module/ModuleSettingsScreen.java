package ru.he3hauka.toolvisuals.client.gui.module;

import net.minecraft.client.gui.screens.Screen;

import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class ModuleSettingsScreen extends VisualsScreen {
    public final VisualModule module;
    public ModuleSettingsScreen(Screen parent, VisualModule module) { super(module); this.module = module; }
}

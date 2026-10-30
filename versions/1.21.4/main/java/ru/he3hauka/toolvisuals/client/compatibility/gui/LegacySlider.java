package ru.he3hauka.toolvisuals.client.compatibility.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public abstract class LegacySlider extends AbstractSliderButton {
    protected LegacySlider(int x,int y,int width,int height,Component label,double value){super(x,y,width,height,label,value);}
    protected void handleCursor(GuiGraphics graphics){if(active&&isHoveredOrFocused())VersionGui.cursor(VersionGui.Cursor.POINTING_HAND);}
}

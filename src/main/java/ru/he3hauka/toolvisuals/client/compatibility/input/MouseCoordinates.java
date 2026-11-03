package ru.he3hauka.toolvisuals.client.compatibility.input;

import net.minecraft.client.Minecraft;

public final class MouseCoordinates {
    private MouseCoordinates(){}
    public static double x(Minecraft client){return client.mouseHandler.xpos()*client.getWindow().getGuiScaledWidth()/client.getWindow().getScreenWidth();}
    public static double y(Minecraft client){return client.mouseHandler.ypos()*client.getWindow().getGuiScaledHeight()/client.getWindow().getScreenHeight();}
}

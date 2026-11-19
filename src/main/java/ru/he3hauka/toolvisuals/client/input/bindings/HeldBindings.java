package ru.he3hauka.toolvisuals.client.input.bindings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public final class HeldBindings {
    private HeldBindings() {}
    public static void key(KeyMapping mapping,KeyEvent event,int action){
        if(mapping!=null&&action!=InputConstants.REPEAT&&mapping.matches(event))mapping.setDown(action==InputConstants.PRESS);
    }
    public static void mouse(KeyMapping mapping,MouseButtonEvent event,int action){
        if(mapping!=null&&action!=InputConstants.REPEAT&&mapping.matchesMouse(event))mapping.setDown(action==InputConstants.PRESS);
    }
}

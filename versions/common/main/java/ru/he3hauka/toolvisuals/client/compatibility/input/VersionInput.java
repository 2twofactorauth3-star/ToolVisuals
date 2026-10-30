package ru.he3hauka.toolvisuals.client.compatibility.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class VersionInput {
    private VersionInput() {}
    public static boolean matches(net.minecraft.client.KeyMapping binding,InputConstants.Key key){return net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.getBoundKeyOf(binding).equals(key);}
    public static boolean down(int key) {return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(),key)==GLFW.GLFW_PRESS;}
    public static boolean shift() {return down(InputConstants.KEY_LSHIFT)||down(InputConstants.KEY_RSHIFT);}
}

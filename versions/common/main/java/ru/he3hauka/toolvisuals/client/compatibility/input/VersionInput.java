package ru.he3hauka.toolvisuals.client.compatibility.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class VersionInput {
    private VersionInput() {}
    public static boolean matches(KeyMapping binding,InputConstants.Key key){return KeyBindingHelper.getBoundKeyOf(binding).equals(key);}
    public static boolean down(int key) {return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(),key)==GLFW.GLFW_PRESS;}
    public static boolean shift() {return down(InputConstants.KEY_LSHIFT)||down(InputConstants.KEY_RSHIFT);}
}

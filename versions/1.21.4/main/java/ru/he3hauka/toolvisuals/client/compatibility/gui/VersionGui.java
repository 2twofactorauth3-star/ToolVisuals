package ru.he3hauka.toolvisuals.client.compatibility.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public final class VersionGui {
    public enum Cursor {
        DEFAULT(GLFW.GLFW_ARROW_CURSOR),POINTING_HAND(GLFW.GLFW_HAND_CURSOR),TEXT(GLFW.GLFW_IBEAM_CURSOR),RESIZE_NWSE(GLFW.GLFW_RESIZE_NWSE_CURSOR),RESIZE_EW(GLFW.GLFW_HRESIZE_CURSOR),RESIZE_NS(GLFW.GLFW_VRESIZE_CURSOR);
        public final int shape;
        private long handle;
        Cursor(int shape){this.shape=shape;}
    }
    private VersionGui() {}
    public static void centered(net.minecraft.client.gui.GuiGraphics graphics,int x,int y,net.minecraft.network.chat.Component text){graphics.drawCenteredString(Minecraft.getInstance().font,text,x,y,0xffffffff);}
    public static void potionIcon(net.minecraft.client.gui.GuiGraphics graphics,net.minecraft.client.renderer.texture.TextureAtlasSprite sprite,int x,int y,int width,int height){graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured,sprite.atlasLocation(),x,y,sprite.getU0()*256,sprite.getV0()*256,width,height,Math.round((sprite.getU1()-sprite.getU0())*256),Math.round((sprite.getV1()-sprite.getV0())*256),256,256,0xffffffff);}
    public static Vector2f transform(PoseStack pose,float x,float y,Vector2f target){var matrix=pose.last().pose();return target.set(matrix.m00()*x+matrix.m10()*y+matrix.m30(),matrix.m01()*x+matrix.m11()*y+matrix.m31());}
    public static void cursor(Cursor cursor){if(cursor.handle==0)cursor.handle=GLFW.glfwCreateStandardCursor(cursor.shape);GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(),cursor.handle);}
}

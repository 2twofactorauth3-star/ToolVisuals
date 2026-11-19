package ru.he3hauka.toolvisuals.client.compatibility.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class LegacyRenderState implements AutoCloseable {
    private final int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
    private final int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
    private final int[] viewport = new int[4];
    private final int[] scissor = new int[4];
    private final boolean clipped = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
    private final boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    private final int depthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
    private final boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final int[] colorWrite = new int[4];
    private final Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
    private final ProjectionType type = RenderSystem.getProjectionType();
    public LegacyRenderState() {
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport); GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
        GL11.glGetIntegerv(GL11.GL_COLOR_WRITEMASK, colorWrite);
        RenderSystem.disableScissor();
        RenderSystem.depthMask(true); RenderSystem.colorMask(true, true, true, true);
    }
    @Override public void close() {
        RenderSystem.setProjectionMatrix(projection, type);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
        RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        RenderSystem.depthFunc(depthFunction);
        RenderSystem.depthMask(depthWrite);
        RenderSystem.colorMask(colorWrite[0] != 0, colorWrite[1] != 0, colorWrite[2] != 0, colorWrite[3] != 0);
        if (depthTest) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        if (clipped) RenderSystem.enableScissor(scissor[0], scissor[1], scissor[2], scissor[3]);
        else RenderSystem.disableScissor();
    }
}

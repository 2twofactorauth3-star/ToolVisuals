package ru.he3hauka.toolvisuals.client.items.highlight.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import org.joml.Vector2f;

public record ItemGlowQuad(int centerX, int centerY, float pixels, float radius) {
    public interface State { void toolvisuals$itemGlowQuad(ItemGlowQuad quad); }
    public void build(BlitRenderState state, VertexConsumer vertices) {
        vertex(state, vertices, state.x0(), state.y0(), state.u0(), state.v0());
        vertex(state, vertices, state.x0(), state.y1(), state.u0(), state.v1());
        vertex(state, vertices, state.x1(), state.y1(), state.u1(), state.v1());
        vertex(state, vertices, state.x1(), state.y0(), state.u1(), state.v0());
    }
    private void vertex(BlitRenderState state, VertexConsumer vertices, int x, int y, float u, float v) {
        var point = state.pose().transformPosition(x, y, new Vector2f());
        vertices.addVertex(point.x, point.y, 0).setColor(state.color()).setUv(u, v).setUv1(Math.round(pixels), Math.round(Math.min(radius, 127.99f) * 256)).setUv2(centerX, centerY).setNormal(0, 0, 1);
    }
}

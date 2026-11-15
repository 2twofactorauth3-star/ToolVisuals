package ru.he3hauka.toolvisuals.client.items.highlight.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

public final class DiscardingVertices implements VertexConsumer {
    public static final DiscardingVertices INSTANCE = new DiscardingVertices();
    private DiscardingVertices() {}
    @Override public VertexConsumer addVertex(float x, float y, float z) { return this; }
    @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
    @Override public VertexConsumer setColor(int color) { return this; }
    @Override public VertexConsumer setUv(float u, float v) { return this; }
    @Override public VertexConsumer setUv1(int u, int v) { return this; }
    @Override public VertexConsumer setUv2(int u, int v) { return this; }
    @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    public VertexConsumer setUv3(float u, float v) { return this; }
    public VertexConsumer setLineWidth(float width) { return this; }
}

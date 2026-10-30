package ru.he3hauka.toolvisuals.client.compatibility.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class GeometryCollector {
    @FunctionalInterface public interface Geometry {void render(PoseStack.Pose matrix,VertexConsumer vertices);}
    public final MultiBufferSource buffers;
    private final org.joml.Matrix4f view;
    public GeometryCollector(MultiBufferSource buffers){this(buffers,null);}
    public GeometryCollector(MultiBufferSource buffers,org.joml.Matrix4f view){this.buffers=buffers;this.view=view;}
    public void submitCustomGeometry(PoseStack pose,RenderType type,Geometry geometry){if(view==null){geometry.render(pose.last(),buffers.getBuffer(type));return;}var transformed=new PoseStack();transformed.mulPose(view);transformed.mulPose(pose.last().pose());geometry.render(transformed.last(),buffers.getBuffer(type));}
}

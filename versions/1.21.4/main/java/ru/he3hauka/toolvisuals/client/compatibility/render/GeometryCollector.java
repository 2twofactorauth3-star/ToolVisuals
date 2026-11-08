package ru.he3hauka.toolvisuals.client.compatibility.render;

import java.util.LinkedHashSet;
import java.util.Set;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class GeometryCollector implements AutoCloseable {
    @FunctionalInterface public interface Geometry {void render(PoseStack.Pose matrix,VertexConsumer vertices);}
    public final MultiBufferSource buffers;
    private final Set<RenderType> submitted=new LinkedHashSet<>();
    public GeometryCollector(MultiBufferSource buffers){this.buffers=buffers;}
    public void submitCustomGeometry(PoseStack pose,RenderType type,Geometry geometry){
        submitted.add(type);
        geometry.render(pose.last(),buffers.getBuffer(type));
    }
    @Override public void close(){
        try{if(buffers instanceof MultiBufferSource.BufferSource source)for(var type:submitted)source.endBatch(type);}
        finally{submitted.clear();}
    }
}

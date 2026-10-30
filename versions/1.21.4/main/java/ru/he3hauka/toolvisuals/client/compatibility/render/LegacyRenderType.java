package ru.he3hauka.toolvisuals.client.compatibility.render;

import java.util.Optional;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class LegacyRenderType extends RenderType {
    private final RenderType original;
    public LegacyRenderType(String name,RenderType original,Runnable setup){super(name,original.format(),original.mode(),original.bufferSize(),original.affectsCrumbling(),original.sortOnUpload(),()->{original.setupRenderState();setup.run();},original::clearRenderState);this.original=original;}
    public LegacyRenderType(String name,VertexFormat format,VertexFormat.Mode mode,int size,boolean sorted,Runnable setup,Runnable clear){super(name,format,mode,size,false,sorted,setup,clear);original=null;}
    @Override public Optional<RenderType> outline(){return original==null?Optional.empty():original.outline();}
    @Override public boolean isOutline(){return original!=null&&original.isOutline();}
}

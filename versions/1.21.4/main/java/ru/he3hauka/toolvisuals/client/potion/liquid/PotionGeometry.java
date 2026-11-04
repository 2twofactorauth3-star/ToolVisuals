package ru.he3hauka.toolvisuals.client.potion.liquid;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;

public final class PotionGeometry {
    private static final LiquidQuadCache<BakedQuad> CACHE=new LiquidQuadCache<>(new LiquidQuadCache.Codec<>(){
        public boolean liquid(BakedQuad quad){return quad.getTintIndex()==0;}
        public boolean edge(BakedQuad quad){return quad.getDirection().getAxis()!=Direction.Axis.Z;}
        public LiquidClipper.Vertex[] vertices(BakedQuad quad){var result=new LiquidClipper.Vertex[4];var packed=quad.getVertices();int stride=packed.length/4;for(int i=0;i<4;i++){int at=i*stride;result[i]=new LiquidClipper.Vertex(Float.intBitsToFloat(packed[at]),Float.intBitsToFloat(packed[at+1]),Float.intBitsToFloat(packed[at+2]),Float.intBitsToFloat(packed[at+4]),Float.intBitsToFloat(packed[at+5]));}return result;}
        public BakedQuad build(BakedQuad source,LiquidClipper.Vertex[] vertices){var old=source.getVertices();int stride=old.length/4;int[] data=old.clone();for(int i=0;i<4;i++){int at=i*stride;var v=vertices[i];data[at]=Float.floatToRawIntBits(v.x());data[at+1]=Float.floatToRawIntBits(v.y());data[at+2]=Float.floatToRawIntBits(v.z());data[at+4]=Float.floatToRawIntBits(v.u());data[at+5]=Float.floatToRawIntBits(v.v());}return new BakedQuad(data,source.getTintIndex(),source.getDirection(),source.getSprite(),source.isShade(),source.getLightEmission());}
    });
    private static final class DrainingModel extends DelegateBakedModel {
        final float fill;
        DrainingModel(BakedModel source,float fill){super(source);this.fill=fill;}
        @Override public List<BakedQuad> getQuads(net.minecraft.world.level.block.state.BlockState state,Direction side,net.minecraft.util.RandomSource random){return CACHE.drain(super.getQuads(state,side,random),fill);}
    }
    private PotionGeometry(){}
    public static void mark(net.minecraft.client.renderer.item.ItemStackRenderState state,float fill){}
    public static BakedModel drain(BakedModel source,float fill){return new DrainingModel(source,fill);}
}

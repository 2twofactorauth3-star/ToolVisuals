package ru.he3hauka.toolvisuals.client.potion.liquid;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class PotionGeometry {
    private static final LiquidQuadCache<BakedQuad> CACHE=new LiquidQuadCache<>(new LiquidQuadCache.Codec<>(){
        public boolean liquid(BakedQuad quad){return quad.tintIndex()==0;}
        public boolean edge(BakedQuad quad){return quad.direction().getAxis()!=Direction.Axis.Z;}
        public LiquidClipper.Vertex[] vertices(BakedQuad quad){var result=new LiquidClipper.Vertex[4];for(int i=0;i<4;i++){var p=quad.position(i);long uv=quad.packedUV(i);result[i]=new LiquidClipper.Vertex(p.x(),p.y(),p.z(),LiquidClipper.low(uv),LiquidClipper.high(uv));}return result;}
        public BakedQuad build(BakedQuad source,LiquidClipper.Vertex[] v){return new BakedQuad(point(v[0]),point(v[1]),point(v[2]),point(v[3]),uv(v[0]),uv(v[1]),uv(v[2]),uv(v[3]),source.tintIndex(),source.direction(),source.sprite(),source.shade(),source.lightEmission());}
    });
    private PotionGeometry(){}
    public static void mark(net.minecraft.client.renderer.item.ItemStackRenderState state,float fill){state.setAnimated();state.appendModelIdentityElement(Math.round(fill*64));}
    private static Vector3f point(LiquidClipper.Vertex v){return new Vector3f(v.x(),v.y(),v.z());}
    private static long uv(LiquidClipper.Vertex v){return LiquidClipper.packed(v.u(),v.v());}
    public static List<BakedQuad> drain(List<BakedQuad> source,float fill){return CACHE.drain(source,fill);}
}

package ru.he3hauka.toolvisuals.client.potion.liquid;

import java.util.*;

public final class LiquidClipper {
    public record Vertex(float x,float y,float z,float u,float v){
        Vertex interpolate(Vertex other,float t){return new Vertex(x+(other.x-x)*t,y+(other.y-y)*t,z+(other.z-z)*t,u+(other.u-u)*t,v+(other.v-v)*t);}
    }
    private LiquidClipper(){}
    public static List<Vertex[]> clip(Vertex[] quad,float height){
        if(quad.length!=4||!Float.isFinite(height))throw new IllegalArgumentException("Invalid liquid quad");
        var polygon=new ArrayList<Vertex>(5);Vertex previous=quad[3];boolean wasBelow=previous.y<=height;
        for(var vertex:quad){
            boolean below=vertex.y<=height;if(below!=wasBelow){float t=(height-previous.y)/(vertex.y-previous.y);polygon.add(previous.interpolate(vertex,t));}
            if(below)polygon.add(vertex);previous=vertex;wasBelow=below;
        }
        if(polygon.size()<3)return List.of();
        if(polygon.size()==4)return List.<Vertex[]>of(polygon.toArray(Vertex[]::new));
        var output=new ArrayList<Vertex[]>();for(int i=1;i<polygon.size()-1;i++)output.add(new Vertex[]{polygon.getFirst(),polygon.get(i),polygon.get(i+1),polygon.get(i+1)});return output;
    }
    public static float low(long packed){return Float.intBitsToFloat((int)packed);}
    public static float high(long packed){return Float.intBitsToFloat((int)(packed>>>32));}
    public static long packed(float low,float high){return Integer.toUnsignedLong(Float.floatToRawIntBits(low))|((long)Float.floatToRawIntBits(high)<<32);}
}

package ru.he3hauka.toolvisuals.client.potion.liquid;

import java.util.*;

public final class LiquidQuadCache<Q> {
    public interface Codec<Q> {
        boolean liquid(Q quad);
        boolean edge(Q quad);
        LiquidClipper.Vertex[] vertices(Q quad);
        Q build(Q source,LiquidClipper.Vertex[] vertices);
    }
    private record Key<Q>(List<Q> source,int step){}
    private final Codec<Q> codec;
    private final LinkedHashMap<Key<Q>,List<Q>> cache=new LinkedHashMap<>(128,.75f,true);
    private int size;
    public LiquidQuadCache(Codec<Q> codec){this.codec=codec;}
    public synchronized List<Q> drain(List<Q> source,float fill){
        if(!Float.isFinite(fill)||fill>=1||source.size()>256)return source;
        int step=Math.clamp(Math.round(fill*64),0,64);var key=new Key<>(List.copyOf(source),step);var cached=cache.get(key);if(cached!=null)return cached;
        float low=Float.POSITIVE_INFINITY,high=Float.NEGATIVE_INFINITY;boolean edges=source.stream().anyMatch(q->codec.liquid(q)&&codec.edge(q));
        for(var quad:source)if(codec.liquid(quad)&&(!edges||codec.edge(quad)))for(var v:codec.vertices(quad)){low=Math.min(low,v.y());high=Math.max(high,v.y());}
        if(!Float.isFinite(low)||high<=low)return source;float height=low+(high-low)*step/64f;var result=new ArrayList<Q>();
        for(var quad:source){
            if(!codec.liquid(quad)){result.add(quad);continue;}if(step==0)continue;
            var vertices=codec.vertices(quad);boolean clipped=false;for(var vertex:vertices)clipped|=vertex.y()>height;
            if(!clipped)result.add(quad);else for(var polygon:LiquidClipper.clip(vertices,height))result.add(codec.build(quad,polygon));
        }
        var value=List.copyOf(result);while(!cache.isEmpty()&&(size+value.size()>16384||cache.size()>=256)){var first=cache.entrySet().iterator();size-=first.next().getValue().size();first.remove();}
        cache.put(key,value);size+=value.size();return value;
    }
}

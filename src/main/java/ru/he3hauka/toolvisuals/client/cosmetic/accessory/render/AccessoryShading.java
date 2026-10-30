package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

import java.util.*;

import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;

final class AccessoryShading {
    private static final int LIMIT=1024;
    private static final Map<ColorPalette,Entry> CACHE=new IdentityHashMap<>();
    private static final ArrayDeque<ColorPalette> ORDER=new ArrayDeque<>();
    private static final class Entry {int[] tones,paints;}
    private AccessoryShading(){}
    static synchronized int[] paints(ColorPalette style,double seconds){
        var entry=CACHE.get(style);
        if(entry==null){entry=new Entry();CACHE.put(style,entry);ORDER.addLast(style);if(ORDER.size()>LIMIT)CACHE.remove(ORDER.removeFirst());}
        int[] next=null;
        for(int i=0;i<8;i++){
            int color=style.sample(seconds,i/7d);
            if(entry.tones==null||entry.tones[i]!=color){if(next==null)next=entry.tones==null?new int[8]:entry.tones.clone();next[i]=color;}
        }
        if(next!=null){
            var paints=new int[256];
            for(int tone=0;tone<8;tone++)for(int shade=0;shade<32;shade++){
                int rgb=next[tone],result=0xff000000;
                for(int shift=0;shift<=16;shift+=8)result|=Math.round((rgb>>shift&255)*shade/31f)<<shift;
                paints[tone*32+shade]=result;
            }
            entry.tones=next;entry.paints=paints;
        }
        return entry.paints;
    }
}

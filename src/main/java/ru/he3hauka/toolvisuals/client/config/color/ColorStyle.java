package ru.he3hauka.toolvisuals.client.config.color;

import java.util.*;

import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;

public final class ColorStyle extends ColorPalette {
    private static final java.util.concurrent.ConcurrentHashMap<Integer,ColorStyle> SOLID=new java.util.concurrent.ConcurrentHashMap<>();
    public ColorStyle(Collection<Integer> colors,Mode mode,Space space,double duration,double hold,double phase,double spread,boolean smooth,boolean reverse){super(colors,mode,space,duration,hold,phase,spread,smooth,reverse);}
    public static ColorStyle solid(int rgb){
        rgb&=0xffffff;var old=SOLID.get(rgb);if(old!=null)return old;
        var style=new ColorStyle(List.of(rgb),Mode.STATIC,Space.RGB,4,0,0,1,true,false);if(SOLID.size()<256)SOLID.putIfAbsent(rgb,style);return style;
    }
    public static ColorStyle from(ColorPalette p){return new ColorStyle(p.colors,p.mode,p.space,p.duration,p.hold,p.phase,p.spread,p.smooth,p.reverse);}
    public static ColorStyle safe(ColorStyle style,int fallback){return style==null?solid(fallback):from(style);}
}

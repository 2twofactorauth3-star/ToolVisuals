package ru.he3hauka.toolvisuals.cosmetic.style;

import java.util.*;

public class ColorPalette {
    public enum Mode { STATIC(0),CYCLE(1),PING_PONG(2);public final int id;Mode(int id){this.id=id;}public static Mode byId(int id){return switch(id){case 0->STATIC;case 1->CYCLE;case 2->PING_PONG;default->throw new IllegalArgumentException("Unknown palette mode");};}}
    public enum Space { RGB(0),HSV(1);public final int id;Space(int id){this.id=id;}public static Space byId(int id){return switch(id){case 0->RGB;case 1->HSV;default->throw new IllegalArgumentException("Unknown palette space");};}}
    public static final int LIMIT = 32;
    public final List<Integer> colors;
    public final Mode mode;
    public final Space space;
    public final double duration, hold, phase, spread;
    public final boolean smooth, reverse;
    private final transient float[][] hsv;

    public ColorPalette(Collection<Integer> colors, Mode mode, Space space, double duration, double hold, double phase, double spread, boolean smooth, boolean reverse) {
        var safe = new ArrayList<Integer>();
        if (colors != null) for (Integer color : colors) {
            if (color != null) safe.add(color & 0xffffff);
            if (safe.size() == LIMIT) break;
        }
        if (safe.isEmpty()) safe.add(0xffffff);
        this.colors = List.copyOf(safe);
        hsv=new float[safe.size()][];for(int i=0;i<safe.size();i++){int rgb=safe.get(i);hsv[i]=rgbToHsv(rgb>>16&255,rgb>>8&255,rgb&255);}
        this.mode = mode == null ? Mode.STATIC : mode;
        this.space = space == null ? Space.RGB : space;
        this.duration = bounded(duration, .2, 120, 4);
        this.hold = bounded(hold, 0, .9, 0);
        this.phase = bounded(phase, 0, 1, 0);
        this.spread = bounded(spread, 0, 2, 1);
        this.smooth = smooth; this.reverse = reverse;
    }
    private static double bounded(double value, double min, double max, double fallback) { return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback; }
    public static ColorPalette solid(int rgb){return new ColorPalette(List.of(rgb),Mode.STATIC,Space.RGB,4,0,0,1,true,false);}
    public static ColorPalette safe(ColorPalette style, int fallback) {
        return style == null ? solid(fallback) : new ColorPalette(style.colors, style.mode, style.space, style.duration, style.hold, style.phase, style.spread, style.smooth, style.reverse);
    }
    public static double clock() { return System.nanoTime() / 1_000_000_000d; }
    public int sample(double seconds, double position) {
        if (colors.size() == 1) return colors.getFirst();
        double time = mode == Mode.STATIC ? 0 : (Double.isFinite(seconds) ? seconds : 0) / duration;
        double p = phase + time + (Double.isFinite(position) ? position : 0) * spread;
        if (mode == Mode.STATIC) return segment(Math.clamp(p, 0, 1) * (colors.size() - 1), false);
        p -= Math.floor(p);
        if (mode == Mode.PING_PONG) return segment((1 - Math.abs(p * 2 - 1)) * (colors.size() - 1), false);
        return segment(p * colors.size(), true);
    }
    private int segment(double position, boolean wrap) {
        int index = Math.min((int)position, colors.size() - 1);
        double pause=mode==Mode.STATIC?0:hold;
        double t = Math.clamp((position - index - pause) / (1 - pause), 0, 1);
        if (smooth) t = t * t * (3 - 2 * t);
        int next=wrap?(index+1)%colors.size():Math.min(index+1,colors.size()-1);
        if(reverse){index=colors.size()-1-index;next=colors.size()-1-next;}
        return space==Space.HSV?mixHsv(hsv[index],hsv[next],t):mix(colors.get(index),colors.get(next),t,space);
    }
    public static int mix(int first, int last, double t, Space space) {
        t = Double.isFinite(t) ? Math.clamp(t, 0, 1) : 0;
        if (t == 0) return first & 0xffffff;
        if (t == 1) return last & 0xffffff;
        if (space == Space.HSV) {
            float[] a = rgbToHsv(first>>16&255,first>>8&255,first&255);
            float[] b = rgbToHsv(last>>16&255,last>>8&255,last&255);
            return mixHsv(a,b,t);
        }
        int rgb = 0;
        for (int shift=0;shift<=16;shift+=8) rgb |= Math.round((first >> shift & 255) + ((last >> shift & 255) - (first >> shift & 255)) * (float)t) << shift;
        return rgb;
    }
    private static int mixHsv(float[] a,float[] b,double t) {
        float start=a[1]<.001f?b[0]:a[0],end=b[1]<.001f?start:b[0],difference=end-start;
        if(difference>.5f)difference-=1;if(difference<-.5f)difference+=1;
        return hsvToRgb(start+difference*(float)t,a[1]+(b[1]-a[1])*(float)t,a[2]+(b[2]-a[2])*(float)t)&0xffffff;
    }
    private static float[] rgbToHsv(int r,int g,int b){
        int max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b));
        float brightness=max/255f,saturation=max==0?0:(max-min)/(float)max,hue;
        if(saturation==0)hue=0;
        else{
            float red=(max-r)/(float)(max-min),green=(max-g)/(float)(max-min),blue=(max-b)/(float)(max-min);
            hue=r==max?blue-green:g==max?2+red-blue:4+green-red;hue/=6;if(hue<0)hue++;
        }
        return new float[]{hue,saturation,brightness};
    }
    private static int hsvToRgb(float hue,float saturation,float brightness){
        int r,g,b;
        if(saturation==0){r=g=b=(int)(brightness*255+.5f);}
        else{
            float h=(hue-(float)Math.floor(hue))*6,f=h-(float)Math.floor(h);
            float p=brightness*(1-saturation),q=brightness*(1-saturation*f),t=brightness*(1-saturation*(1-f));
            float rf,gf,bf;
            switch((int)h){case 0->{rf=brightness;gf=t;bf=p;}case 1->{rf=q;gf=brightness;bf=p;}case 2->{rf=p;gf=brightness;bf=t;}case 3->{rf=p;gf=q;bf=brightness;}case 4->{rf=t;gf=p;bf=brightness;}default->{rf=brightness;gf=p;bf=q;}}
            r=(int)(rf*255+.5f);g=(int)(gf*255+.5f);b=(int)(bf*255+.5f);
        }
        return 0xff000000|r<<16|g<<8|b;
    }
    @Override public boolean equals(Object other){return other instanceof ColorPalette p&&colors.equals(p.colors)&&mode==p.mode&&space==p.space&&Double.compare(duration,p.duration)==0&&Double.compare(hold,p.hold)==0&&Double.compare(phase,p.phase)==0&&Double.compare(spread,p.spread)==0&&smooth==p.smooth&&reverse==p.reverse;}
    @Override public int hashCode(){return Objects.hash(colors,mode,space,duration,hold,phase,spread,smooth,reverse);}
}

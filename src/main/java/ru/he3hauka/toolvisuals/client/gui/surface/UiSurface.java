package ru.he3hauka.toolvisuals.client.gui.surface;

import java.awt.image.BufferedImage;

import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;

public enum UiSurface {
    GLASS(18,16), ROW(10,8), PRIMARY(10,8), ACTIVE(10,8), EMBLEM(10,8);

    public static final int DENSITY=3;
    public final int padding,radius,pixels,corner;

    UiSurface(int padding,int radius) {
        this.padding=padding;
        this.radius=radius;
        pixels=(2*(padding+radius)+24)*DENSITY;
        corner=(padding+radius)*DENSITY;
    }

    public BufferedImage rasterize(boolean light) {
        var image=new BufferedImage(pixels,pixels,BufferedImage.TYPE_INT_ARGB);
        float size=(float)pixels/DENSITY,extent=size-2*padding;
        int top,bottom,rimTop,rimBottom;
        switch(this) {
            case GLASS -> {
                top=light?0xeaf6f2ff:0xe91e1b2d;bottom=light?0xf1eef0f8:0xf011121b;
                rimTop=light?0xb4ffffff:0xa28872b8;rimBottom=light?0x627d6b9e:0x656b4d92;
            }
            case PRIMARY -> {
                top=light?0xf5e7dcfb:0xf2372c50;bottom=light?0xf5f0eaf9:0xf0232133;
                rimTop=light?0xc2ffffff:0x896d568f;rimBottom=light?0x567d649e:0x3e4c3b64;
            }
            case ACTIVE -> {
                top=light?0xffd9c5f6:0xff634299;bottom=light?0xffe9ddfa:0xff382a59;
                rimTop=light?0xffffffff:0xedbc98fa;rimBottom=light?0xa58f69bc:0xae885bc3;
            }
            case EMBLEM -> {
                top=light?0xffe0d0fa:0xff453064;bottom=light?0xfff2ebfc:0xff242033;
                rimTop=light?0xfaffffff:0xb38461ba;rimBottom=light?0x7d9275bb:0x554b366d;
            }
            default -> {
                top=light?0xebffffff:0xeb292738;bottom=light?0xf4f0edf7:0xf31c1d29;
                rimTop=light?0xdaffffff:0x56565072;rimBottom=light?0x4a8d819f:0x2c3b354f;
            }
        }
        float half=extent/2,center=size/2;
        for(int py=0;py<pixels;py++)for(int px=0;px<pixels;px++) {
            float x=(px+.5f)/DENSITY,y=(py+.5f)/DENSITY;
            float dx=Math.abs(x-center)-half+radius,dy=Math.abs(y-center)-half+radius;
            float distance=(float)Math.hypot(Math.max(dx,0),Math.max(dy,0))+Math.min(Math.max(dx,dy),0)-radius;
            float horizontal=Math.clamp((x-padding)/extent,0,1),vertical=Math.clamp((y-padding)/extent,0,1);
            float diagonal=vertical*.78f+horizontal*.22f;
            int fill=UiPalette.mix(top,bottom,diagonal),rim=UiPalette.mix(rimTop,rimBottom,diagonal);
            if(this==GLASS) {
                float glow=(float)(Math.exp(-7*(horizontal*horizontal+vertical*vertical))*.17
                        +Math.exp(-9*((1-horizontal)*(1-horizontal)+(1-vertical)*(1-vertical)))*.10);
                int wash=UiPalette.mix(fill,light?0xffc9b2f1:0xff7650a8,glow);
                fill=fill&0xff000000|wash&0xffffff;
            }
            float coverage=Math.clamp(.5f-distance*DENSITY,0,1);
            float inside=Math.clamp(.5f-(distance+.7f)*DENSITY,0,1);
            int surface=over(UiPaint.alpha(fill,inside),UiPaint.alpha(rim,coverage-inside));
            float outside=Math.max(0,distance),sigma=this==GLASS?5:3;
            float glow=(float)Math.exp(-outside*outside/(2*sigma*sigma));
            int shade=this==ACTIVE?0x429763e6:this==EMBLEM?0x288b59d8:this==GLASS?0x22000000:0x10000000;
            image.setRGB(px,py,over(surface,UiPaint.alpha(shade,glow)));
        }
        return image;
    }

    private static int over(int front,int back) {
        float a=(front>>>24)/255f,b=(back>>>24)/255f*(1-a),sum=a+b;
        if(sum==0)return 0;
        int result=Math.round(sum*255)<<24;
        for(int shift=0;shift<=16;shift+=8)
            result|=Math.round(((front>>>shift&255)*a+(back>>>shift&255)*b)/sum)<<shift;
        return result;
    }
}

package ru.he3hauka.toolvisuals.client.gui.surface;

import java.awt.image.BufferedImage;

public final class RoundedGlow {
    public static final int PIXELS=256;

    @FunctionalInterface
    public interface Region {
        void accept(float x,float y,float width,float height);
    }

    private RoundedGlow() {}

    public static BufferedImage rasterize() {
        var image=new BufferedImage(PIXELS,PIXELS,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<PIXELS;y++)for(int x=0;x<PIXELS;x++) {
            double dx=(x+.5)*2/PIXELS-1,dy=(y+.5)*2/PIXELS-1;
            double falloff=Math.max(0,1-dx*dx-dy*dy);
            image.setRGB(x,y,(int)Math.round(255*falloff*falloff*falloff)<<24|0xffffff);
        }
        return image;
    }

    public static boolean contains(float x,float y,float width,float height,float radius,float px,float py) {
        if(px<x||py<y||px>x+width||py>y+height)return false;
        float r=Math.min(radius,Math.min(width,height)/2);
        float nearestX=Math.clamp(px,x+r,x+width-r),nearestY=Math.clamp(py,y+r,y+height-r);
        return Math.hypot(px-nearestX,py-nearestY)<=r;
    }

    public static void visit(float x,float y,float width,float height,float radius,float cx,float cy,float glowRadius,Region region) {
        if(width<=0||height<=0||glowRadius<=0)return;
        float r=Math.clamp(radius,0,Math.min(width,height)/2);
        intersect(x,y+r,width,height-2*r,cx,cy,glowRadius,region);
        int bands=Math.max(1,(int)Math.ceil(r));
        for(int band=0;band<bands;band++) {
            float top=r*band/bands,bottom=r*(band+1)/bands;
            float inset=r-(float)Math.sqrt(Math.max(0,r*r-(r-top)*(r-top)))+.001f;
            intersect(x+inset,y+top,width-2*inset,bottom-top,cx,cy,glowRadius,region);
            intersect(x+inset,y+height-bottom,width-2*inset,bottom-top,cx,cy,glowRadius,region);
        }
    }

    private static void intersect(float x,float y,float width,float height,float cx,float cy,float radius,Region region) {
        float left=Math.max(x,cx-radius),top=Math.max(y,cy-radius),right=Math.min(x+width,cx+radius),bottom=Math.min(y+height,cy+radius);
        if(right>left&&bottom>top)region.accept(left,top,right-left,bottom-top);
    }
}

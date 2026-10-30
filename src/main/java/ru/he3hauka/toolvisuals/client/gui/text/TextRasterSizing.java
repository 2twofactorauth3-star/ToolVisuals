package ru.he3hauka.toolvisuals.client.gui.text;

public final class TextRasterSizing {
    public record Offset(float x,float y) {}
    private TextRasterSizing() {}
    public static float pixelRatio(int pixels,int units,int fallback){return pixels>0&&units>0?Math.clamp(pixels/(float)units,.25f,32):Math.max(1,fallback);}
    public static float density(float m00,float m01,float m10,float m11,float xPixels,float yPixels){
        double x=Math.hypot(m00*xPixels,m01*yPixels),y=Math.hypot(m10*xPixels,m11*yPixels);
        double density=Math.max(x,y);return Double.isFinite(density)?(float)Math.clamp(density,.05,32):1;
    }
    public static int pixels(float scale,float density){return Math.clamp((int)Math.ceil(15*Math.max(.01f,scale)*density*2),16,256);}
    public static float rasterDensity(int pixels,float scale){return pixels/(15*Math.max(.01f,scale));}
    public static Offset snap(float x,float y,float m00,float m01,float m10,float m11,float xPixels,float yPixels){
        double determinant=m00*m11-m01*m10;if(!Double.isFinite(determinant)||Math.abs(determinant)<1e-8)return new Offset(0,0);
        double dx=Math.round(x*xPixels)/xPixels-x,dy=Math.round(y*yPixels)/yPixels-y;
        return new Offset((float)((m11*dx-m10*dy)/determinant),(float)((m00*dy-m01*dx)/determinant));
    }
}

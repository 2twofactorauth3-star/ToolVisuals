package ru.he3hauka.toolvisuals.client.cape;

import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

import static ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign.*;

public final class CapePresets {
    public enum Kind { TOOLVISUALS, RUSSIA, KAZAKHSTAN, BELARUS, UKRAINE, SOLID, GRADIENT, STRIPES, PULSE, SHIMMER }
    public static final Kind DEFAULT=Kind.TOOLVISUALS;
    public static final int PRIMARY=0xffb59aff,SECONDARY=0xff302052;
    private CapePresets(){}
    public static CapeDesign create(Kind kind,int primary,int secondary){
        if(kind==Kind.PULSE||kind==Kind.SHIMMER){
            var base=create(Kind.TOOLVISUALS,primary,secondary);var frames=new java.util.ArrayList<CapeDesign>();
            for(int frame=0;frame<MAX_FRAMES;frame++){int[] pixels=base.pixels();for(int i=0;i<pixels.length;i++){
                int y=i%FACE_PIXELS/WIDTH;float amount=kind==Kind.PULSE?(float)(.12+.32*(1+Math.sin(frame*Math.TAU/MAX_FRAMES))/2):Math.max(0,1-Math.abs(Math.floorMod(y+frame*HEIGHT/MAX_FRAMES,HEIGHT)-HEIGHT/2f)/7)*.65f;
                pixels[i]=mix(pixels[i],kind==Kind.PULSE?0xff090518:0xfff4efff,amount);
            }frames.add(new CapeDesign(pixels));}return new CapeDesign(frames,DEFAULT_FPS);
        }
        int[] pixels=new int[PIXELS];primary|=0xff000000;secondary|=0xff000000;
        for(var face:Face.values())for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){
            int color=switch(kind){case SOLID->primary;case STRIPES->((x+y)/4%2==0?primary:secondary);case GRADIENT,TOOLVISUALS->mix(primary,secondary,y/(float)(HEIGHT-1));case RUSSIA,KAZAKHSTAN,BELARUS,UKRAINE->CapeFlags.pixel(kind,x,y);case PULSE,SHIMMER->throw new IllegalStateException("Animated preset already handled");};
            if(kind==Kind.TOOLVISUALS){
                boolean border=x==1||x==WIDTH-2||y==1||y==HEIGHT-2;
                boolean emblem=y>=9&&y<=12&&x>=4&&x<=15||y>=13&&y<=23&&x>=8&&x<=11;
                if(border||emblem)color=0xfff4efff;
            }
            pixels[index(face,x,y)]=color;
        }
        return new CapeDesign(pixels);
    }
    private static int mix(int a,int b,float amount){int color=0xff000000;for(int shift:new int[]{0,8,16})color|=Math.round(((a>>shift)&255)*(1-amount)+((b>>shift)&255)*amount)<<shift;return color;}
}

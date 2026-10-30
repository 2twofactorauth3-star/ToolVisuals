package ru.he3hauka.toolvisuals.client.cape;

import java.util.Arrays;

import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

import static ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign.*;

public final class CapeAtlas {
    public static final int WIDTH=128,HEIGHT=64;
    @FunctionalInterface public interface PixelSink {void pixel(int x,int y,int color);}
    private static final int[] SOURCE=mapping();
    private CapeAtlas(){}
    private static int[] mapping(){
        int[] source=new int[WIDTH*HEIGHT];Arrays.fill(source,-1);
        for(var face:Face.values())for(int y=0;y<CapeDesign.HEIGHT;y++)for(int x=0;x<CapeDesign.WIDTH;x++)source[(y+2)*WIDTH+(face==Face.FRONT?2:24)+x]=index(face,x,y);
        for(int y=0;y<CapeDesign.HEIGHT;y++)for(int edge=0;edge<2;edge++){source[(y+2)*WIDTH+edge]=index(Face.FRONT,0,y);source[(y+2)*WIDTH+22+edge]=index(Face.FRONT,CapeDesign.WIDTH-1,y);}
        for(int x=0;x<CapeDesign.WIDTH;x++)for(int edge=0;edge<2;edge++){source[edge*WIDTH+2+x]=index(Face.FRONT,x,0);source[edge*WIDTH+22+x]=index(Face.FRONT,x,CapeDesign.HEIGHT-1);}
        return source;
    }
    public static void paint(CapeDesign design,PixelSink sink){for(int i=0;i<SOURCE.length;i++)sink.pixel(i%WIDTH,i/WIDTH,SOURCE[i]<0?0:design.pixel(SOURCE[i]));}
}

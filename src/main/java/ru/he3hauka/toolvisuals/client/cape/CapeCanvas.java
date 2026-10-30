package ru.he3hauka.toolvisuals.client.cape;

import java.util.ArrayDeque;
import java.util.Arrays;

import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

import static ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign.*;

public final class CapeCanvas {
    public enum Tool { BRUSH, FILL, PICKER, ERASER }
    private int[] pixels;
    private CapeDesign before,cached;
    private final ArrayDeque<CapeDesign> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    public CapeCanvas(CapeDesign design){pixels=design.pixels();cached=design;}
    public CapeDesign snapshot(){if(cached==null)cached=new CapeDesign(pixels);return cached;}
    public int pixel(Face face,int x,int y){return pixels[index(face,x,y)];}
    public void begin(){if(before==null)before=snapshot();}
    public boolean end(){
        if(before==null)return false;
        boolean changed=!Arrays.equals(before.pixels(),pixels);
        if(changed){undo.addLast(before);if(undo.size()>50)undo.removeFirst();redo.clear();}
        before=null;return changed;
    }
    public void replace(CapeDesign design){begin();pixels=design.pixels();cached=design;end();}
    public boolean canUndo(){return !undo.isEmpty();}
    public boolean canRedo(){return !redo.isEmpty();}
    public void undo(){end();if(undo.isEmpty())return;redo.addLast(snapshot());cached=undo.removeLast();pixels=cached.pixels();}
    public void redo(){if(redo.isEmpty())return;undo.addLast(snapshot());cached=redo.removeLast();pixels=cached.pixels();}
    public void line(Face face,int x0,int y0,int x1,int y1,int color,int radius,boolean mirror){
        if(radius<0||radius>2)throw new IllegalArgumentException("Invalid brush size");
        index(face,x0,y0);index(face,x1,y1);begin();cached=null;
        int dx=Math.abs(x1-x0),sx=x0<x1?1:-1,dy=-Math.abs(y1-y0),sy=y0<y1?1:-1,error=dx+dy;
        while(true){
            for(int y=y0-radius;y<=y0+radius;y++)for(int x=x0-radius;x<=x0+radius;x++)if(x>=0&&x<WIDTH&&y>=0&&y<HEIGHT){pixels[index(face,x,y)]=color;if(mirror)pixels[index(face,WIDTH-1-x,y)]=color;}
            if(x0==x1&&y0==y1)break;int twice=2*error;if(twice>=dy){error+=dy;x0+=sx;}if(twice<=dx){error+=dx;y0+=sy;}
        }
    }
    public void fill(Face face,int x,int y,int color){
        int original=pixel(face,x,y);if(original==color)return;begin();cached=null;
        var queue=new ArrayDeque<Integer>();queue.add(y*WIDTH+x);pixels[index(face,x,y)]=color;
        while(!queue.isEmpty()){
            int point=queue.removeFirst(),px=point%WIDTH,py=point/WIDTH;
            for(int direction=0;direction<4;direction++){
                int nx=px+(direction==0?1:direction==1?-1:0),ny=py+(direction==2?1:direction==3?-1:0);
                if(nx>=0&&nx<WIDTH&&ny>=0&&ny<HEIGHT&&pixel(face,nx,ny)==original){pixels[index(face,nx,ny)]=color;queue.add(ny*WIDTH+nx);}
            }
        }
    }
}

package ru.he3hauka.toolvisuals.client.hud.layout.resize;

import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement.Bounds;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPosition;

public final class HudResize {
    public record Change(HudElement element,double scale,HudPosition position,Bounds bounds){}
    private HudResize(){}

    public static Bounds positioned(Bounds box,HudPosition position,int width,int height){
        if(position==null)return box;
        return new Bounds(Math.max(0,width-box.width)*position.x/100,Math.max(0,height-box.height)*position.y/100,box.width,box.height);
    }

    public static Change change(HudElement element,Bounds box,double current,double wheel,int width,int height,int bottomInset){
        if(box==null||!Float.isFinite(box.x)||!Float.isFinite(box.y)||!Float.isFinite(box.width)||!Float.isFinite(box.height)
                ||!Double.isFinite(wheel)||wheel==0||width<=0||height<=bottomInset||box.width<1||box.height<1)return null;
        current=HudScale.sanitize(current);
        double next=HudScale.next(current,wheel);
        if(wheel>0){
            double fit=current*Math.min(width/(double)box.width,(height-bottomInset)/(double)box.height);
            next=Math.max(current,Math.min(next,Math.floor(fit*10000)/10000d));
        }
        if(next==current)return null;
        float w=(float)(box.width*next/current),h=(float)(box.height*next/current);
        float x=Math.clamp(box.x,0,Math.max(0,width-w)),y=Math.clamp(box.y,0,Math.max(0,height-bottomInset-h));
        var position=HudPosition.fromPixels(x,y,width,height,w,h);
        return new Change(element,next,position,new Bounds(x,y,w,h));
    }
}

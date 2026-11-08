package ru.he3hauka.toolvisuals.client.waypoint.label;

import java.util.ArrayList;
import java.util.List;

public final class WorldLabelLayout {
    public static final class Box {
        public final float x,y,width,height;
        private Box(float x,float y,float width,float height){this.x=x;this.y=y;this.width=width;this.height=height;}
    }
    private static final float MARGIN=2,GAP=4;
    private final float width,height;
    private final List<Box> placed=new ArrayList<>();
    public WorldLabelLayout(float width,float height){this.width=width;this.height=height;}
    public Box place(float anchorX,float anchorY,float boxWidth,float boxHeight){
        if(!Float.isFinite(width)||!Float.isFinite(height)||!Float.isFinite(anchorX)||!Float.isFinite(anchorY)
                ||!Float.isFinite(boxWidth)||!Float.isFinite(boxHeight)||boxWidth<=0||boxHeight<=0
                ||boxWidth>width-2*MARGIN||boxHeight>height-2*MARGIN||placed.size()>=256)return null;
        float x=Math.clamp(anchorX-boxWidth/2,MARGIN,width-boxWidth-MARGIN);
        for(int attempt=0;attempt<17;attempt++){
            int row=(attempt+1)/2;
            float offset=row*(boxHeight+GAP)*(attempt%2==0?-1:1);
            float y=Math.clamp(anchorY-boxHeight+offset,MARGIN,height-boxHeight-MARGIN);
            if(available(x,y,boxWidth,boxHeight)){var box=new Box(x,y,boxWidth,boxHeight);placed.add(box);return box;}
        }
        return null;
    }
    private boolean available(float x,float y,float boxWidth,float boxHeight){
        for(var other:placed)if(x<other.x+other.width+GAP&&x+boxWidth+GAP>other.x
                &&y<other.y+other.height+GAP&&y+boxHeight+GAP>other.y)return false;
        return true;
    }
}

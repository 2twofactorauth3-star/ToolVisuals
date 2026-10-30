package ru.he3hauka.toolvisuals.client.hand.editor;

import java.util.Map;
import ru.he3hauka.toolvisuals.client.hand.HandPose;
import ru.he3hauka.toolvisuals.client.hand.HandProperty;
import ru.he3hauka.toolvisuals.client.hand.HandSide;

public final class HandInteraction {
    private HandSide side;
    private HandPose initial;
    private HandProjection.Frame bounds;
    private double x,y;
    private boolean rotating;

    public void begin(HandSide side,HandPose initial,HandProjection.Frame bounds,double x,double y,boolean rotating) {
        this.side=side;this.initial=initial;this.bounds=bounds;this.x=x;this.y=y;this.rotating=rotating;
    }
    public boolean dragging(){return side!=null;}
    public HandSide side(){return side;}
    public void finish(){side=null;initial=null;bounds=null;}

    public Map<HandProperty,Double> move(double x,double y,boolean precise) {
        if(side==null)return Map.of();
        double factor=precise?.2:1;
        if(rotating)return Map.of(HandProperty.ROTATION_X,(double)initial.rotationX+(y-this.y)*.45*factor,
                HandProperty.ROTATION_Y,(double)initial.rotationY+(x-this.x)*.45*factor);
        var delta=bounds.plane().movement((x-this.x)*factor,(y-this.y)*factor);
        return Map.of(HandProperty.X,initial.x+delta.x,HandProperty.Y,initial.y+delta.y);
    }

    public static double scale(double current,double scroll,boolean precise) {
        if(!Double.isFinite(current)||!Double.isFinite(scroll))return HandProperty.SCALE.right.initial;
        return HandProperty.SCALE.right.sanitize(current*Math.exp(Math.clamp(scroll,-30,30)*(precise?.02:.1)));
    }
}

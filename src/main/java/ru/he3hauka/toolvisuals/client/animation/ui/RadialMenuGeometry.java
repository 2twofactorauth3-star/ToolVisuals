package ru.he3hauka.toolvisuals.client.animation.ui;

public final class RadialMenuGeometry {
    private RadialMenuGeometry(){}
    public static int index(double x,double y,double inner,double outer,int count) {
        if(!Double.isFinite(x)||!Double.isFinite(y)||count<1)return -1;
        double distance=Math.hypot(x,y);if(distance<inner||distance>outer)return -1;
        double step=Math.TAU/count,angle=(Math.atan2(y,x)+Math.PI/2+step/2+Math.TAU)%Math.TAU;
        double local=angle%step;if(local<.018||local>step-.018)return -1;
        return Math.min(count-1,(int)(angle/step));
    }
}

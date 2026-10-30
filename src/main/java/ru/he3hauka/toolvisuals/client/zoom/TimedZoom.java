package ru.he3hauka.toolvisuals.client.zoom;

public final class TimedZoom {
    public static final double MIN_SECONDS=.1;
    public static final double MAX_SECONDS=10;
    public static final double DEFAULT_SECONDS=3.3;
    private boolean held;
    private long changed;
    private double start=1,value=1;
    public float sample(long now,boolean down,double seconds,double factor,boolean smooth){
        if(down!=held){held=down;changed=now;start=value;}
        double target=down?1/Math.clamp(factor,1,64):1;
        double duration=down?Math.clamp(seconds,MIN_SECONDS,MAX_SECONDS):.22;
        double progress=Math.clamp((now-changed)/1e9/duration,0,1);
        double eased=progress*progress*(3-2*progress);
        value=smooth?start*Math.pow(target/start,eased):target;
        return (float)value;
    }
    public float value(){return (float)value;}
    public void reset(){held=false;changed=0;start=value=1;}
    public static double factor(double distance){return Math.clamp(distance/8,2,64);}
}

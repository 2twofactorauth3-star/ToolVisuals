package ru.he3hauka.toolvisuals.client.optimization.frame;

public final class AdaptiveQuality {
    private double average;
    private long previous,overloaded,recovered,changed;
    private int level;
    public int level(){return level;}
    public void reset(){average=0;previous=overloaded=recovered=changed=0;level=0;}
    public void sample(long now,long interval,int targetFps){
        if(interval<=0||interval>250_000_000L||targetFps<1){previous=now;overloaded=recovered=0;return;}
        double elapsed=previous==0?0:Math.clamp((now-previous)/1e9,0,.1);previous=now;
        double weight=1-Math.exp(-elapsed/.6);average=average==0?interval:average+(interval-average)*weight;
        double budget=1e9/targetFps;
        if(average>budget*1.18){
            recovered=0;if(overloaded==0)overloaded=now;
            if(level<2&&now-overloaded>=1_500_000_000L&&now-changed>=2_000_000_000L){level++;changed=now;overloaded=0;}
        }else if(average<budget*1.04){
            overloaded=0;if(recovered==0)recovered=now;
            if(level>0&&now-recovered>=8_000_000_000L){level--;changed=now;recovered=0;}
        }else{overloaded=recovered=0;}
    }
}

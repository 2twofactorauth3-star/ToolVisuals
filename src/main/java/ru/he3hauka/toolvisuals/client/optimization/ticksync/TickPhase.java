package ru.he3hauka.toolvisuals.client.optimization.ticksync;

public final class TickPhase {
    private final double[] delays=new double[10];
    private int count,index;
    private long lastTick,lastCorrection;

    public double sample(long now,long arrival,int fps,double margin,double maximum){
        if(lastTick!=0&&(now-lastTick<20_000_000L||now-lastTick>150_000_000L)){reset();lastTick=now;return 0;}
        lastTick=now;
        if(arrival==0||now<arrival||now-arrival>100_000_000L||fps<=40){count=index=0;return 0;}
        double delay=(now-arrival)/1_000_000d;
        if(delay>=50){count=index=0;return 0;}
        delays[index]=delay;index=(index+1)%delays.length;count=Math.min(count+1,delays.length);
        if(count<delays.length||lastCorrection!=0&&now-lastCorrection<1_000_000_000L)return 0;
        double sum=0,min=50,max=0;
        for(double value:delays){sum+=value;min=Math.min(min,value);max=Math.max(max,value);}
        double mean=sum/count; margin=Math.clamp(margin,4,20);
        if(max-min>8||mean<=margin+4)return 0;
        double shift=Math.min(Math.clamp(maximum,5,40),(50-mean+margin)%50);
        if(shift<1)return 0;
        lastCorrection=now;count=index=0;return shift;
    }
    public void reset(){count=index=0;lastTick=lastCorrection=0;}
}

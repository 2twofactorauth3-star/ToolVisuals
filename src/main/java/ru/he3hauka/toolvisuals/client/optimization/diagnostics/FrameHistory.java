package ru.he3hauka.toolvisuals.client.optimization.diagnostics;

import java.util.Arrays;

public final class FrameHistory {
    public record Snapshot(double[] milliseconds,double average,double p95,double p99,double maximum,int over25,int over50){}
    private final double[] samples;
    private int cursor,size;
    public FrameHistory(int capacity){if(capacity<1)throw new IllegalArgumentException();samples=new double[capacity];}
    public synchronized void add(long nanos){if(nanos<=0)return;samples[cursor]=nanos/1e6;cursor=(cursor+1)%samples.length;size=Math.min(size+1,samples.length);}
    public synchronized void clear(){cursor=size=0;}
    public Snapshot snapshot(){
        double[] values;
        synchronized(this){
            values=new double[size];
            for(int i=0;i<size;i++)values[i]=samples[Math.floorMod(cursor-size+i,samples.length)];
        }
        double total=0,maximum=0;
        int over25=0,over50=0;
        for(double value:values){
            total+=value;maximum=Math.max(maximum,value);
            if(value>25)over25++;
            if(value>50)over50++;
        }
        double[] sorted=values.clone();
        Arrays.sort(sorted);
        return new Snapshot(values,values.length==0?0:total/values.length,percentile(sorted,.95),
                percentile(sorted,.99),maximum,over25,over50);
    }
    private static double percentile(double[] values,double quantile){return values.length==0?0:values[(int)Math.ceil(values.length*quantile)-1];}
}

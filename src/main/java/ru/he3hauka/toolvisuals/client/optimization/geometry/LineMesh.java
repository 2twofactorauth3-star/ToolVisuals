package ru.he3hauka.toolvisuals.client.optimization.geometry;

import java.util.Arrays;

public final class LineMesh {
    @FunctionalInterface public interface Sink {void line(double ax,double ay,double az,double bx,double by,double bz,int color,float width);}
    public static final class Snapshot {
        public static final Snapshot EMPTY=new Snapshot(new double[0],new int[0],new float[0]);
        private final double[] points;
        private final int[] colors;
        private final float[] widths;
        public final int size;
        private Snapshot(double[] points,int[] colors,float[] widths){this.points=points;this.colors=colors;this.widths=widths;size=colors.length;}
        public void emit(Sink sink){for(int i=0;i<size;i++){int p=i*6;sink.line(points[p],points[p+1],points[p+2],points[p+3],points[p+4],points[p+5],colors[i],widths[i]);}}
    }
    private static final int MAXIMUM=131072;
    private double[] points=new double[256*6];
    private int[] colors=new int[256];
    private float[] widths=new float[256];
    private int size;
    public void clear(){size=0;}
    public int size(){return size;}
    public void line(double ax,double ay,double az,double bx,double by,double bz,int color,float width){
        if(size>=MAXIMUM)return;
        if(size==colors.length){int capacity=Math.min(MAXIMUM,size*2);points=Arrays.copyOf(points,capacity*6);colors=Arrays.copyOf(colors,capacity);widths=Arrays.copyOf(widths,capacity);}
        int p=size*6;points[p]=ax;points[p+1]=ay;points[p+2]=az;points[p+3]=bx;points[p+4]=by;points[p+5]=bz;colors[size]=color;widths[size++]=width;
    }
    public Snapshot snapshot(){return size==0?Snapshot.EMPTY:new Snapshot(Arrays.copyOf(points,size*6),Arrays.copyOf(colors,size),Arrays.copyOf(widths,size));}
}

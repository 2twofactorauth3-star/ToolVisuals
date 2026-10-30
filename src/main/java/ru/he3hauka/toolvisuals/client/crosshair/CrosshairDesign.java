package ru.he3hauka.toolvisuals.client.crosshair;

import java.util.*;

public final class CrosshairDesign {
    public static final int SIZE = 33, CENTER = SIZE / 2;
    public final List<String> rows;
    public final List<Run> runs;
    public static final class Run {
        public final int x, y, length;
        Run(int x, int y, int length) { this.x=x; this.y=y; this.length=length; }
    }
    public CrosshairDesign(Collection<String> source) {
        var rows = new ArrayList<String>();
        var input = source == null ? List.<String>of() : new ArrayList<>(source);
        var runs = new ArrayList<Run>();
        for (int y=0; y<SIZE; y++) {
            String line = y < input.size() && input.get(y) != null ? input.get(y) : "";
            var row = new StringBuilder(); int start = -1;
            for (int x=0; x<=SIZE; x++) {
                boolean ink = x<SIZE && x<line.length() && line.charAt(x)=='1';
                if (x<SIZE) row.append(ink?'1':'0');
                if (ink && start<0) start=x;
                if (!ink && start>=0) { runs.add(new Run(start,y,x-start)); start=-1; }
            }
            rows.add(row.toString());
        }
        this.rows=List.copyOf(rows);this.runs=List.copyOf(runs);
    }
    public boolean pixel(int x,int y) { return x>=0&&x<SIZE&&y>=0&&y<SIZE&&rows.get(y).charAt(x)=='1'; }
    public static CrosshairDesign empty() { return new CrosshairDesign(List.of()); }
    public static CrosshairDesign cross() {
        var draft = new Draft(empty());
        for(int i=3;i<=8;i++) { draft.paint(CENTER+i,CENTER,1,true,false,false);draft.paint(CENTER-i,CENTER,1,true,false,false);draft.paint(CENTER,CENTER+i,1,true,false,false);draft.paint(CENTER,CENTER-i,1,true,false,false); }
        return draft.snapshot();
    }
    public static final class Draft {
        private final boolean[][] pixels = new boolean[SIZE][SIZE];
        public Draft(CrosshairDesign design) { for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)pixels[y][x]=design.pixel(x,y); }
        public void paint(int x,int y,int brush,boolean ink,boolean mirrorX,boolean mirrorY) {
            int radius=Math.clamp(brush,1,5)/2;
            for(int dy=-radius;dy<=radius;dy++)for(int dx=-radius;dx<=radius;dx++) {
                dot(x+dx,y+dy,ink);if(mirrorX)dot(SIZE-1-x-dx,y+dy,ink);if(mirrorY)dot(x+dx,SIZE-1-y-dy,ink);if(mirrorX&&mirrorY)dot(SIZE-1-x-dx,SIZE-1-y-dy,ink);
            }
        }
        private void dot(int x,int y,boolean ink) { if(x>=0&&x<SIZE&&y>=0&&y<SIZE)pixels[y][x]=ink; }
        public void line(int x0,int y0,int x1,int y1,int brush,boolean ink,boolean mirrorX,boolean mirrorY) {
            int distance=Math.max(Math.abs(x1-x0),Math.abs(y1-y0));
            for(int i=0;i<=distance;i++) { float t=distance==0?0:i/(float)distance;paint(Math.round(x0+(x1-x0)*t),Math.round(y0+(y1-y0)*t),brush,ink,mirrorX,mirrorY); }
        }
        public CrosshairDesign snapshot() {
            var rows=new ArrayList<String>();for(var line:pixels){var row=new StringBuilder();for(boolean ink:line)row.append(ink?'1':'0');rows.add(row.toString());}return new CrosshairDesign(rows);
        }
    }
}

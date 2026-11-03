package ru.he3hauka.toolvisuals.cosmetic.cape;

import java.util.ArrayList;

public final class CapeAnimationPolicy {
    public static final int MAX_FRAMES=4, MAX_FPS=5, ANIMATION_DISTANCE=24;
    public static final long UPLOAD_INTERVAL=1_000_000_000L/MAX_FPS;
    public static final long UPDATE_INTERVAL=5_000_000_000L;

    private CapeAnimationPolicy() {}

    public static CapeDesign limit(CapeDesign design) {
        if(design==null||design.frameCount==1||design.frameCount<=MAX_FRAMES&&design.fps<=MAX_FPS)return design;
        int count=Math.min(MAX_FRAMES,design.frameCount);
        int fps=Math.clamp((int)Math.round((double)design.fps*count/design.frameCount),1,MAX_FPS);
        var frames=new ArrayList<CapeDesign>(count);
        for(int i=0;i<count;i++)frames.add(design.frame(i*design.frameCount/count));
        return new CapeDesign(frames,fps);
    }

    public static CapeDesign frame(CapeDesign design,long now) {
        int fps=Math.min(MAX_FPS,design.fps);
        long seconds=Math.floorDiv(now,1_000_000_000L);
        long part=Math.floorMod(now,1_000_000_000L)*fps/1_000_000_000L;
        return design.frame((int)((Math.floorMod(seconds,design.frameCount)*fps+part)%design.frameCount));
    }

    public static boolean animate(boolean own,double distanceSquared) {
        return own||Double.isFinite(distanceSquared)&&distanceSquared<=ANIMATION_DISTANCE*ANIMATION_DISTANCE;
    }
    public static boolean uploadDue(long lastUpload,long now) {
        return lastUpload==Long.MIN_VALUE||now-lastUpload>=UPLOAD_INTERVAL;
    }
}

package ru.he3hauka.toolvisuals.client.hud.animation;

public final class OpeningTransition {
    private Object owner;
    private long started;
    public void open(Object owner,long now){if(this.owner!=owner){this.owner=owner;started=now;}}
    public float progress(long now,double seconds){
        if(owner==null)return 1;
        double progress=Math.clamp((now-started)/1e9/Math.max(.001,seconds),0,1);
        return (float)(1-Math.pow(1-progress,3));
    }
    public void clear(){owner=null;started=0;}
}

package ru.he3hauka.toolvisuals.client.hud.health;

public final class HitHealthState {
    private double started=Double.NEGATIVE_INFINITY;
    private int duration;
    private float health,absorption;
    public void hit(double now,int duration){this.started=now;this.duration=Math.clamp(duration,10,600);}
    public void update(float health,float maximum,float absorption){
        float limit=Float.isFinite(maximum)?Math.clamp(maximum,1,1024):20;
        this.health=Float.isFinite(health)?Math.clamp(health,0,limit):0;
        this.absorption=Float.isFinite(absorption)?Math.clamp(absorption,0,1024):0;
    }
    public boolean active(double now){return Double.isFinite(started)&&now>=started&&now-started<duration;}
    public float health(){return health;}
    public float absorption(){return absorption;}
    public void clear(){started=Double.NEGATIVE_INFINITY;health=absorption=0;}
}

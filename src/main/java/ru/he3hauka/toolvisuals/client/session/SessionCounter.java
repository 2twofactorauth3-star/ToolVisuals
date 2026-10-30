package ru.he3hauka.toolvisuals.client.session;

public final class SessionCounter {
    public record Snapshot(long seconds,long deaths,long blocks,double distance){}
    private Object connection,world,player;
    private long started,deaths,blocks;
    private double distance,x,z;
    private boolean sampled,dead,deathCounted;

    public void scope(Object connection,long now){if(this.connection==connection)return;this.connection=connection;reset(now);}
    public void reset(long now){started=now;deaths=blocks=0;distance=0;sampled=dead=deathCounted=false;world=player=null;}
    public void observe(Object world,Object player,boolean dead,double x,double z){
        if(connection==null||world==null||player==null){sampled=false;return;}
        if(this.player!=player)deathCounted=false;
        if(sampled&&this.player==player){if(dead&&!this.dead&&!deathCounted){deaths++;deathCounted=true;}if(this.world==world&&!dead&&!this.dead){double moved=Math.hypot(x-this.x,z-this.z);if(Double.isFinite(moved)&&moved<=8)distance+=moved;}}
        this.world=world;this.player=player;this.dead=dead;this.x=x;this.z=z;sampled=true;
    }
    public void broken(){if(connection!=null)blocks++;}
    public void death(Object life){if(connection==null||life==null)return;if(player!=life){sampled=false;deathCounted=false;}if(!deathCounted)deaths++;player=life;dead=deathCounted=true;}
    public Snapshot snapshot(long now){return new Snapshot(connection==null?0:Math.max(0,(now-started)/1_000_000_000L),deaths,blocks,distance);}
}

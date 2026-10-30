package ru.he3hauka.toolvisuals.client.effects;

import java.util.ArrayDeque;
import java.util.Arrays;

import net.minecraft.world.phys.Vec3;

public final class EffectTimeline {
    public record Circle(Vec3 center,long started,int duration,float radius) {}
    public record TrailPoint(Vec3 position,double started) {}
    public record Sprite(Vec3 origin,Vec3 velocity,long started,int duration,float size,int kind,float phase,boolean hit) {}
    public record Snapshot(Circle[] circles,TrailPoint[] trail,Sprite[] ambient,Sprite[] hits) {
        public static final Snapshot EMPTY=new Snapshot(new Circle[0],new TrailPoint[0],new Sprite[0],new Sprite[0]);
        public boolean empty(){return circles.length+trail.length+ambient.length+hits.length==0;}
    }
    private final ArrayDeque<Circle> circles=new ArrayDeque<>();
    private final ArrayDeque<TrailPoint> trail=new ArrayDeque<>();
    private final ArrayDeque<Sprite> ambient=new ArrayDeque<>(),hits=new ArrayDeque<>();
    public void clear(){circles.clear();trail.clear();ambient.clear();hits.clear();}
    public void tick(long now,int circleLimit,int spriteLimit,int trailDuration,boolean showCircles,boolean showTrail,boolean showAmbient,boolean showHits){
        circles.removeIf(circle->!showCircles||now-circle.started>=circle.duration);
        trail.removeIf(point->!showTrail||now-point.started>=trailDuration);
        ambient.removeIf(sprite->!showAmbient||now-sprite.started>=sprite.duration);
        hits.removeIf(sprite->!showHits||now-sprite.started>=sprite.duration);
        while(circles.size()>Math.clamp(circleLimit,0,16))circles.removeFirst();
        while(spriteCount()>Math.clamp(spriteLimit,0,4096)){if(!ambient.isEmpty())ambient.removeFirst();else hits.removeFirst();}
    }
    public void jump(Vec3 center,long now,int duration,float radius,int limit){
        limit=Math.clamp(limit,0,16);if(limit==0)return;
        while(circles.size()>=limit)circles.removeFirst();
        circles.addLast(new Circle(center,now,Math.clamp(duration,1,200),Math.clamp(radius,.1f,4)));
    }
    public void trail(Vec3 from,Vec3 to,long now,int density){
        double distance=from.distanceToSqr(to);
        if(distance>16){trail.clear();return;}
        if(distance<.002)return;
        if(!trail.isEmpty()&&(trail.getLast().position.distanceToSqr(from)>1||now-trail.getLast().started>2))trail.clear();
        if(trail.isEmpty())trail.addLast(new TrailPoint(from,now-1));
        int samples=Math.clamp(density,1,5);
        for(int i=1;i<=samples;i++)trail.addLast(new TrailPoint(from.lerp(to,i/(double)samples),now-1+i/(double)samples));
        while(trail.size()>256)trail.removeFirst();
    }
    public void sprite(Sprite sprite,int limit){
        limit=Math.clamp(limit,0,4096);if(limit==0)return;
        if(!sprite.hit&&spriteCount()>=limit)return;
        while(spriteCount()>=limit){if(!ambient.isEmpty())ambient.removeFirst();else hits.removeFirst();}
        (sprite.hit?hits:ambient).addLast(sprite);
    }
    public int spriteCount(){return ambient.size()+hits.size();}
    public Snapshot snapshot(){return snapshot(null,0);}
    public Snapshot snapshot(Vec3 head,double trailTime){
        if(circles.isEmpty()&&trail.isEmpty()&&ambient.isEmpty()&&hits.isEmpty())return Snapshot.EMPTY;
        var points=trail.toArray(TrailPoint[]::new);
        if(head!=null&&points.length>0&&points[points.length-1].position.distanceToSqr(head)<1){
            trailTime=Math.min(trailTime,points[points.length-1].started);
            int count=points.length;while(count>0&&points[count-1].started>trailTime)count--;
            points=Arrays.copyOf(points,count+1);points[count]=new TrailPoint(head,trailTime);
        }
        return new Snapshot(circles.toArray(Circle[]::new),points,ambient.toArray(Sprite[]::new),hits.toArray(Sprite[]::new));
    }
}

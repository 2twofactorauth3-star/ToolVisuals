package ru.he3hauka.toolvisuals.client.projectile;

import java.util.*;

import net.minecraft.world.phys.*;

public final class Ballistics {
    public interface Environment {
        HitResult clip(Vec3 from,Vec3 to);
        double drag(Vec3 position);
        boolean loaded(Vec3 position);
        default boolean gravityBeforeMove(){return false;}
        default boolean dragBeforeMove(Vec3 position){return false;}
    }
    public static final class Path {
        public final List<Vec3> points;
        public final HitResult hit;
        Path(List<Vec3> points,HitResult hit){this.points=List.copyOf(points);this.hit=hit;}
    }
    private Ballistics() {}
    public static Path trace(Vec3 origin,Vec3 velocity,double gravity,Environment environment,int maximumTicks) {
        var points=new ArrayList<Vec3>();points.add(origin);Vec3 current=origin;
        for(int tick=0;tick<Math.clamp(maximumTicks,1,200);tick++) {
            boolean gravityFirst=environment.gravityBeforeMove();
            boolean dragFirst=gravityFirst||environment.dragBeforeMove(current);
            double drag=environment.drag(current);
            if(gravityFirst)velocity=velocity.add(0,-gravity,0);
            if(dragFirst)velocity=velocity.scale(drag);
            Vec3 next=current.add(velocity);if(!environment.loaded(next))break;
            var hit=environment.clip(current,next);
            if(hit!=null&&hit.getType()!=HitResult.Type.MISS){points.add(hit.getLocation());return new Path(points,hit);}
            points.add(next);current=next;
            if(!dragFirst)velocity=velocity.scale(drag);
            if(!gravityFirst)velocity=velocity.add(0,-gravity,0);
        }
        return new Path(points,null);
    }
}

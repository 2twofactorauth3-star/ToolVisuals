package ru.he3hauka.toolvisuals.client.effects;

import net.minecraft.world.phys.Vec3;

import ru.he3hauka.toolvisuals.client.optimization.geometry.UnitCircle;

public final class EffectGeometry {
    @FunctionalInterface public interface VertexSink {void vertex(float x,float y,float z,int color);}
    public record Frame(EffectTimeline.Snapshot effects,double time,Vec3 camera,Vec3 right,Vec3 up,int circleColor,int trailColor,int ambientColor,int hitColor,float circleWidth,float glow,float trailSize,int trailDuration,int quality) {}
    private EffectGeometry() {}
    public static void emit(Frame frame,VertexSink sink){
        for(var circle:frame.effects.circles())circle(frame,circle,sink);
        trail(frame,sink);
        for(var sprite:frame.effects.ambient())sprite(frame,sprite,frame.ambientColor,sink);
        for(var sprite:frame.effects.hits())sprite(frame,sprite,frame.hitColor,sink);
    }
    public static double progress(double now,double started,int duration){return Math.clamp((now-started)/Math.max(1,duration),0,1);}
    public static double circleOpacity(double progress){return Math.min(1,progress*8)*Math.pow(1-progress,1.5);}
    private static int tint(int rgb,double opacity){return Math.clamp((int)Math.round(opacity*255),0,255)<<24|rgb&0xffffff;}
    private static int pale(int rgb){return ((rgb>>16&255)+255)/2<<16|((rgb>>8&255)+255)/2<<8|((rgb&255)+255)/2;}
    private static void circle(Frame frame,EffectTimeline.Circle circle,VertexSink sink){
        double t=progress(frame.time,circle.started(),circle.duration()),opacity=circleOpacity(t);
        if(opacity<.002)return;
        double radius=circle.radius()*(.12+.88*(1-Math.pow(1-t,3))),width=frame.circleWidth;
        Vec3 center=circle.center().subtract(frame.camera);
        int segments=EffectBudget.segments(center.lengthSqr(),64,frame.quality);
        int clear=tint(frame.circleColor,0),soft=tint(frame.circleColor,opacity*.45*frame.glow),bright=tint(pale(frame.circleColor),opacity*.9);
        band(center,radius-width*(1+frame.glow*3),radius-width*.35,clear,soft,segments,sink);
        band(center,radius-width*.35,radius,soft,bright,segments,sink);
        band(center,radius,radius+width*.35,bright,soft,segments,sink);
        band(center,radius+width*.35,radius+width*(1+frame.glow*3),soft,clear,segments,sink);
    }
    private static void band(Vec3 center,double inner,double outer,int innerColor,int outerColor,int segments,VertexSink sink){
        inner=Math.max(.005,inner);outer=Math.max(inner+.001,outer);
        double px=1,pz=0;
        for(int i=1;i<=segments;i++){
            double x=UnitCircle.x(i,segments),z=UnitCircle.z(i,segments);
            vertex(sink,center.x+px*inner,center.y,center.z+pz*inner,innerColor);
            vertex(sink,center.x+px*outer,center.y,center.z+pz*outer,outerColor);
            vertex(sink,center.x+x*outer,center.y,center.z+z*outer,outerColor);
            vertex(sink,center.x+x*inner,center.y,center.z+z*inner,innerColor);
            px=x;pz=z;
        }
    }
    private static void trail(Frame frame,VertexSink sink){
        var points=frame.effects.trail();
        if(points.length<2)return;
        Vec3 last=points[0].position().subtract(frame.camera).add(0,.14,0);
        Vec3 side=trailSide(points,0,frame.camera,frame.right);
        double alpha=trailOpacity(frame,points[0]),width=frame.trailSize*(.25+.75*alpha)*.18;
        for(int i=1;i<points.length;i++){
            Vec3 next=points[i].position().subtract(frame.camera).add(0,.14,0),nextSide=trailSide(points,i,frame.camera,frame.right);
            if(side.dot(nextSide)<0)nextSide=nextSide.scale(-1);
            double nextAlpha=trailOpacity(frame,points[i]),nextWidth=frame.trailSize*(.25+.75*nextAlpha)*.18;
            if(last.distanceToSqr(next)<1){
                ribbon(last,next,side,nextSide,width,nextWidth,-1,0,tint(frame.trailColor,0),tint(frame.trailColor,alpha*.65),tint(frame.trailColor,0),tint(frame.trailColor,nextAlpha*.65),sink);
                ribbon(last,next,side,nextSide,width,nextWidth,0,1,tint(frame.trailColor,alpha*.65),tint(frame.trailColor,0),tint(frame.trailColor,nextAlpha*.65),tint(frame.trailColor,0),sink);
                ribbon(last,next,side,nextSide,width*.16,nextWidth*.16,-1,1,tint(pale(frame.trailColor),alpha*.65),tint(pale(frame.trailColor),alpha*.65),tint(pale(frame.trailColor),nextAlpha*.65),tint(pale(frame.trailColor),nextAlpha*.65),sink);
            }
            last=next;side=nextSide;alpha=nextAlpha;width=nextWidth;
        }
    }
    private static double trailOpacity(Frame frame,EffectTimeline.TrailPoint point){return Math.pow(1-progress(frame.time,point.started(),frame.trailDuration),1.7);}
    private static Vec3 trailSide(EffectTimeline.TrailPoint[] points,int index,Vec3 camera,Vec3 fallback){
        Vec3 tangent=points[Math.min(points.length-1,index+1)].position().subtract(points[Math.max(0,index-1)].position());
        Vec3 side=tangent.cross(points[index].position().subtract(camera));
        return side.lengthSqr()<1e-8?fallback:side.normalize();
    }
    private static void ribbon(Vec3 a,Vec3 b,Vec3 aside,Vec3 bside,double awidth,double bwidth,double inner,double outer,int ai,int ao,int bi,int bo,VertexSink sink){
        point(sink,a,aside,awidth*inner,ai);point(sink,a,aside,awidth*outer,ao);point(sink,b,bside,bwidth*outer,bo);point(sink,b,bside,bwidth*inner,bi);
    }
    private static void point(VertexSink sink,Vec3 at,Vec3 side,double scale,int color){vertex(sink,at.x+side.x*scale,at.y+side.y*scale,at.z+side.z*scale,color);}
    private static void sprite(Frame frame,EffectTimeline.Sprite sprite,int color,VertexSink sink){
        double age=Math.max(0,frame.time-sprite.started()),t=progress(frame.time,sprite.started(),sprite.duration());
        if(t>=1)return;
        double opacity=Math.min(1,age/2)*Math.pow(1-t,1.4),travel=(1-Math.pow(.96,age))/.04;
        double gravity=sprite.hit()?.0008:0;
        Vec3 at=sprite.origin().add(sprite.velocity().scale(travel)).add(Math.sin(age*.07+sprite.phase())*.035,-gravity*age*age,0).subtract(frame.camera);
        double radius=sprite.size()*(.6+.4*Math.sin(Math.PI*t)),angle=sprite.phase()+age*(sprite.kind()==1?.035:.012);
        double cosine=Math.cos(angle),sine=Math.sin(angle);
        if(sprite.kind()==2){
            for(int i=0;i<3;i++){
                double theta=angle+i*Math.PI/3,dx=Math.cos(theta)*radius,dy=Math.sin(theta)*radius;
                billboardQuad(frame,at,-dx-dy*.045,-dy+dx*.045,dx-dy*.045,dy+dx*.045,dx+dy*.045,dy-dx*.045,-dx+dy*.045,-dy-dx*.045,tint(color,opacity*.8),sink);
            }
            for(int i=0;i<6;i++)for(int side=-1;side<=1;side+=2){
                double theta=angle+i*Math.PI/3,dx=Math.cos(theta)*radius,dy=Math.sin(theta)*radius;
                double ax=dx*.55,ay=dy*.55,bx=dx*.8-dy*.22*side,by=dy*.8+dx*.22*side;
                double wx=-(by-ay)*.075,wy=(bx-ax)*.075;
                billboardQuad(frame,at,ax-wx,ay-wy,bx-wx,by-wy,bx+wx,by+wy,ax+wx,ay+wy,tint(color,opacity*.7),sink);
            }
        }else{
            int center=tint(pale(color),opacity*.9),edge=tint(color,sprite.kind()==1?opacity*.2:0);
            for(int i=0;i<8;i++){
                double a=i*Math.PI/4,b=(i+1)*Math.PI/4;
                double ar=radius*(sprite.kind()==1?1:i%2==0?1:.3),br=radius*(sprite.kind()==1?1:(i+1)%2==0?1:.3);
                double ax=Math.cos(a)*ar,ay=Math.sin(a)*ar,bx=Math.cos(b)*br,by=Math.sin(b)*br;
                if(sprite.kind()==1){ax*=.55;bx*=.55;}
                if(sprite.kind()==3){ax*=.3;bx*=.3;ay*=1.8;by*=1.8;}
                billboard(frame,at,0,0,center,sink);
                billboard(frame,at,ax*cosine-ay*sine,ax*sine+ay*cosine,edge,sink);
                billboard(frame,at,bx*cosine-by*sine,bx*sine+by*cosine,edge,sink);
                billboard(frame,at,0,0,center,sink);
            }
        }
    }
    private static void billboardQuad(Frame frame,Vec3 at,double ax,double ay,double bx,double by,double cx,double cy,double dx,double dy,int color,VertexSink sink){
        billboard(frame,at,ax,ay,color,sink);billboard(frame,at,bx,by,color,sink);billboard(frame,at,cx,cy,color,sink);billboard(frame,at,dx,dy,color,sink);
    }
    private static void billboard(Frame frame,Vec3 at,double x,double y,int color,VertexSink sink){vertex(sink,at.x+frame.right.x*x+frame.up.x*y,at.y+frame.right.y*x+frame.up.y*y,at.z+frame.right.z*x+frame.up.z*y,color);}
    private static void vertex(VertexSink sink,double x,double y,double z,int color){sink.vertex((float)x,(float)y,(float)z,color);}
}

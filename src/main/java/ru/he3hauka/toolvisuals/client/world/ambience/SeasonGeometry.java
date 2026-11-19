package ru.he3hauka.toolvisuals.client.world.ambience;

import net.minecraft.world.phys.Vec3;
import ru.he3hauka.toolvisuals.client.effects.EffectGeometry;
import ru.he3hauka.toolvisuals.client.effects.EffectTimeline;
import ru.he3hauka.toolvisuals.client.optimization.geometry.UnitCircle;

public final class SeasonGeometry {
    private static final double[][] MAPLE={{0,-1},{-.18,-.45},{-.6,-.55},{-.46,-.12},{-.94,.12},{-.57,.27},{-.69,.68},{-.23,.51},{0,1},{.23,.51},{.69,.68},{.57,.27},{.94,.12},{.46,-.12},{.6,-.55},{.18,-.45}};
    private static final double[][] LEAF={{0,-1},{-.3,-.68},{-.52,-.2},{-.48,.29},{-.29,.69},{0,1},{.29,.69},{.48,.29},{.52,-.2},{.3,-.68}};
    private static final double[][] PETAL={{0,-1},{-.39,-.57},{-.63,-.03},{-.61,.48},{-.36,.82},{-.12,.79},{0,.57},{.12,.79},{.36,.82},{.61,.48},{.63,-.03},{.39,-.57}};
    private SeasonGeometry() {}

    public static void emit(EffectGeometry.Frame frame,EffectTimeline.Sprite[] sprites,int color,EffectGeometry.VertexSink sink){
        for(var sprite:sprites){
            double age=frame.time()-sprite.started(),life=age/sprite.duration();
            if(life<=0||life>=1)continue;
            double fade=smooth(Math.min(life/.12,(1-life)/.25));
            double wind=Math.sin(age*.043+sprite.phase())*.22,flutter=Math.sin(age*.071+sprite.phase());
            Vec3 at=sprite.origin().add(sprite.velocity().scale(age)).add(wind,flutter*(sprite.kind()==0?.12:.035),Math.cos(age*.029+sprite.phase())*.12).subtract(frame.camera());
            double distance=at.length();if(distance<=2||distance>32)continue;
            fade*=smooth((distance-2)/1.5);
            double angle=sprite.phase()+age*(sprite.kind()==2?.008:sprite.kind()==0?.003:.018);
            double fold=sprite.kind()==4||sprite.kind()==5?.35+.65*Math.abs(Math.cos(sprite.phase()+age*.035)):1;
            var brush=new Brush(frame,at,sprite.size(),angle,fold,sink);
            if(sprite.kind()==4)leaf(brush,sprite.phase(),fade);
            else if(sprite.kind()==5)petal(brush,fade);
            else if(sprite.kind()==2)snow(brush,color,fade,distance<10||frame.quality()==0);
            else mote(brush,color,fade*(.55+.45*Math.pow(.5+.5*Math.sin(age*.11+sprite.phase()),2)));
        }
    }
    private static double smooth(double value){value=Math.clamp(value,0,1);return value*value*(3-2*value);}
    private static int tint(int color,double alpha){return Math.clamp((int)Math.round(alpha*255),0,255)<<24|color&0xffffff;}
    private static final class Brush {
        private final EffectGeometry.Frame frame;
        private final Vec3 at;
        private final double size,cosine,sine,fold;
        private final EffectGeometry.VertexSink sink;
        Brush(EffectGeometry.Frame frame,Vec3 at,double size,double angle,double fold,EffectGeometry.VertexSink sink){this.frame=frame;this.at=at;this.size=size;cosine=Math.cos(angle);sine=Math.sin(angle);this.fold=fold;this.sink=sink;}
        void point(double x,double y,int color){
            double u=(x*fold*cosine-y*sine)*size,v=(x*fold*sine+y*cosine)*size;
            sink.vertex((float)(at.x+frame.right().x*u+frame.up().x*v),(float)(at.y+frame.right().y*u+frame.up().y*v),(float)(at.z+frame.right().z*u+frame.up().z*v),color);
        }
        void quad(double ax,double ay,double bx,double by,double cx,double cy,double dx,double dy,int a,int b,int c,int d){point(ax,ay,a);point(bx,by,b);point(cx,cy,c);point(dx,dy,d);}
        void line(double ax,double ay,double bx,double by,double width,int color,double opacity){
            double length=Math.hypot(bx-ax,by-ay);if(length<.00001)return;
            double x=-(by-ay)/length*width,y=(bx-ax)/length*width;
            int clear=tint(color,0),core=tint(color,opacity);
            quad(ax-x*2,ay-y*2,bx-x*2,by-y*2,bx-x,by-y,ax-x,ay-y,clear,clear,core,core);
            quad(ax-x,ay-y,bx-x,by-y,bx+x,by+y,ax+x,ay+y,core,core,core,core);
            quad(ax+x,ay+y,bx+x,by+y,bx+x*2,by+y*2,ax+x*2,ay+y*2,core,core,clear,clear);
        }
    }
    private static void surface(Brush brush,double[][] contour,int center,int edge,double opacity){
        for(int i=0;i<contour.length;i++){
            var a=contour[i];var b=contour[(i+1)%contour.length];
            int shade=tint(edge,opacity*.65),middle=tint(center,opacity);
            brush.quad(0,0,a[0],a[1],b[0],b[1],0,0,middle,shade,shade,middle);
            brush.quad(a[0],a[1],a[0]*1.07,a[1]*1.07,b[0]*1.07,b[1]*1.07,b[0],b[1],shade,tint(edge,0),tint(edge,0),shade);
        }
    }
    private static void leaf(Brush brush,double phase,double opacity){
        int variant=(int)(phase*10)%3;
        int center=variant==0?0xf9bd54:variant==1?0xe99535:0xe46f31;
        int edge=variant==0?0x9d5b20:variant==1?0xa94a22:0x93342a;
        surface(brush,variant==0?MAPLE:LEAF,center,edge,opacity*.92);
        brush.line(0,-1.18,0,.82,.013,0x7f4b26,opacity*.65);
        for(int i=0;i<3;i++)for(int side=-1;side<=1;side+=2){double y=-.45+i*.34;brush.line(0,y,side*(.38-i*.055),y+.25,.0065,0xffda81,opacity*.45);}
    }
    private static void petal(Brush brush,double opacity){
        surface(brush,PETAL,0xffe5ed,0xe285a8,opacity*.86);
        brush.line(0,-.8,-.14,.4,.008,0xfff1f4,opacity*.4);
    }
    private static void snow(Brush brush,int color,double opacity,boolean detailed){
        halo(brush,1.16,color,opacity*.065);
        for(int i=0;i<6;i++){
            double x=UnitCircle.x(i,6),y=UnitCircle.z(i,6);
            brush.line(0,0,x,y,.022,0xf1f8ff,opacity*.92);
            if(detailed)for(int side=-1;side<=1;side+=2)brush.line(x*.48,y*.48,x*.76-y*.23*side,y*.76+x*.23*side,.014,0xe1efff,opacity*.85);
        }
        halo(brush,.16,0xffffff,opacity*.8);
    }
    private static void mote(Brush brush,int color,double opacity){
        halo(brush,2.4,color,opacity*.1);halo(brush,1.15,0xe4ef9c,opacity*.3);halo(brush,.4,0xfff6c5,opacity*.92);
    }
    private static void halo(Brush brush,double radius,int color,double opacity){
        for(int i=0;i<12;i++)brush.quad(0,0,UnitCircle.x(i,12)*radius,UnitCircle.z(i,12)*radius,UnitCircle.x(i+1,12)*radius,UnitCircle.z(i+1,12)*radius,0,0,tint(color,opacity),tint(color,0),tint(color,0),tint(color,opacity));
    }
}

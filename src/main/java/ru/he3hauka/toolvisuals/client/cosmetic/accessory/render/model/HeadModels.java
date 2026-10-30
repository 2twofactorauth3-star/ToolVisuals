package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.*;
import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;

import static ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model.MeshPrimitives.*;

public final class HeadModels {
    private HeadModels(){}
    public static AccessoryModel build(AccessoryLook.Head type,boolean far){
        var b=new AccessoryMesh.Builder();int split;
        switch(type){
            case NONE -> split=0;
            case HORNS -> {horns(b,far);split=b.mark();}
            case CROWN -> {crown(b,far);split=b.mark();}
            case HALO -> split=halo(b,far);
            case CAT_EARS -> {ears(b,far);split=b.mark();}
            case FOX_EARS -> {HeadAccessories.fox(b,far);split=b.mark();}
            case ANTLERS -> {HeadAccessories.antlers(b,far);split=b.mark();}
            case WITCH_HAT -> {HeadAccessories.witch(b,far);split=b.mark();}
            case HEADPHONES -> {HeadAccessories.headphones(b,far);split=b.mark();}
            case FLOWER_CROWN -> {HeadAccessories.flowers(b,far);split=b.mark();}
            case ASTROLABE -> split=HeadAccessories.astrolabe(b,far);
            default -> throw new IllegalArgumentException("Unknown head model");
        }
        return new AccessoryModel(b.build(),split,0,type==AccessoryLook.Head.ASTROLABE?-.88:-.76,0);
    }
    private static Curve horn(int side){return t->bezier(p(side*.19,-.49,.06),p(side*.58,-.66,.08),p(side*.52,-1.04,-.23),p(side*.28,-1.03,-.18),t);}
    private static void horns(AccessoryMesh.Builder b,boolean far){
        for(int side:new int[]{-1,1}){
            var path=horn(side);
            tube(b,path,far?7:20,far?5:9,t->.092*Math.pow(1-t,1.15)*(1+.08*Math.sin(t*Math.PI*12))+.0015,0,5);
            var root=path.at(.02);var tangent=path.at(.05).subtract(path.at(0));
            torus(b,root,tangent,.091,.014,far?8:16,far?3:5,7);
            if(!far){
                torus(b,path.at(.10),tangent,.081,.007,14,4,6);
                for(int i=1;i<=4;i++){
                    double t=i*.15;var normal=path.at(t+.01).subtract(path.at(t-.01));double radius=.092*Math.pow(1-t,1.15);
                    torus(b,path.at(t),normal,radius,.0045*(1-t)+.0015,12,3,Math.clamp(5+i/2,0,7));
                }
                pipe(b,t->path.at(.12+t*.72).add(p(side*.011,0,-.018*(1-t))),12,3,.003,7);
                var charm=root.add(p(side*.09,.005,-.065));gem(b,charm.add(p(0,.044,0)),charm.add(p(0,-.035,0)),.024,6,7);
            }
        }
    }
    private static void crown(AccessoryMesh.Builder b,boolean far){
        torus(b,p(0,-.535,0),p(0,1,0),.292,.022,far?20:40,far?4:6,3);
        if(!far)torus(b,p(0,-.573,0),p(0,1,0),.287,.0085,40,4,7);
        int count=far?3:5;
        for(int i=0;i<count;i++){
            double angle=-Math.PI/2+(i-(count-1)/2d)*(far?.63:.40),x=Math.cos(angle)*.292,z=Math.sin(angle)*.292;
            double middle=1-Math.abs(i-(count-1)/2d)/Math.max(1,(count-1)/2d),height=.16+.12*middle;
            var root=p(x,-.55,z);var tip=p(x*.85,-.55-height,z*.85);
            gem(b,mix(root,tip,.14),mix(root,tip,.67),.028+.011*middle,far?5:7,tone(.10+.50*middle));
            if(!far){
                gem(b,tip.add(p(0,.017,0)),tip.add(p(0,-.018,0)),.010,5,7);
                var tangent=p(-Math.sin(angle),0,Math.cos(angle));
                for(int side:new int[]{-1,1}){
                    var start=root.add(tangent.multiply(side*.073));var bend=mix(start,tip,.45).add(tangent.multiply(side*.025));
                    pipe(b,t->bezier(start,bend,tip.add(p(0,.045,0)),tip,t),5,4,.008,7);
                }
                torus(b,root.add(p(0,-.035,0)),p(x,0,z),.034,.0055,10,3,7);
            }
        }
        if(!far){
            for(int i=0;i<8;i++){
                double angle=i*Math.TAU/8;var center=p(Math.cos(angle)*.297,-.544,Math.sin(angle)*.297);
                gem(b,center.add(p(0,.024,0)),center.add(p(0,-.026,0)),.013,5,7);
            }
        }
    }
    private static int halo(AccessoryMesh.Builder b,boolean far){
        var center=p(0,-.77,0);
        torus(b,center,p(.12,1,.055),.337,.014,far?20:40,far?4:6,3);
        if(!far){
            torus(b,center.add(p(0,-.024,0)),p(.12,1,.055),.324,.0045,40,3,7);
            for(int i=0;i<8;i++){
                double angle=(i+.5)*Math.TAU/8;var point=center.add(p(Math.cos(angle)*.339,-.012,Math.sin(angle)*.339));
                gem(b,point.add(p(0,.023,0)),point.add(p(0,-.036,0)),.017,5,i%2==0?7:0);
            }
        }
        int split=b.mark();
        torus(b,center.add(p(0,.013,0)),p(-.18,1,-.04),.252,.0085,far?16:32,far?3:5,7);
        if(!far){
            for(int i=0;i<6;i++){
                double angle=i*Math.TAU/6;
                pipe(b,t->{double a=angle+(t-.5)*.27,r=.254+.036*Math.sin(t*Math.PI);return center.add(p(Math.cos(a)*r,.013,Math.sin(a)*r));},5,3,.0045,2);
            }
            gem(b,center.add(p(0,.075,0)),center.add(p(0,-.054,0)),.027,6,0);
        }
        return split;
    }
    private static void ears(AccessoryMesh.Builder b,boolean far){
        pipe(b,t->p((t-.5)*.42,-.512-.014*Math.sin(t*Math.PI),.08),far?4:10,far?3:5,.012,2);
        for(int side:new int[]{-1,1}){
            double x=side*.21;
            Point[] outline={p(x-side*.117,-.510,-.085),p(x-side*.090,-.640,-.078),p(x+side*.032,-.827,-.01),p(x+side*.115,-.632,-.067),p(x+side*.124,-.510,-.075),p(x,-.492,-.105)};
            var center=p(x+side*.017,-.59,-.025);int segments=far?12:24,radial=far?2:3;
            var front=new Point[radial+1][segments+1];var back=new Point[3][segments+1];
            for(int r=0;r<=radial;r++)for(int c=0;c<=segments;c++){
                double radius=r/(double)radial;var edge=closedSpline(outline,c/(double)segments);
                front[r][c]=mix(center,edge,radius).add(p(0,0,-.013*Math.sin(radius*Math.PI)));
            }
            grid(b,front,(r,u)->r>.80?1:6,true);
            for(int r=0;r<3;r++)for(int c=0;c<=segments;c++){
                double radius=r/2d;var edge=closedSpline(outline,c/(double)segments);
                back[r][c]=mix(center.add(p(0,0,.12)),edge,radius).add(p(0,0,.03*Math.sin(radius*Math.PI)));
            }
            grid(b,back,(r,u)->tone(.05+.50*r),false);
            pipe(b,t->closedSpline(outline,t),segments,far?3:5,.013,1);
            if(!far){
                pipe(b,t->mix(center,closedSpline(outline,t),.72).add(p(0,0,-.014)),segments,3,.006,7);
                for(int i=0;i<3;i++){
                    var root=p(x-side*(.072+i*.015),-.51,-.04);var tip=root.add(p(-side*(.025+i*.008),-.052+i*.01,-.025));
                    feather(b,root,tip,.016,.010,3,5,7);
                }
                if(side==1){
                    var charm=p(x+.113,-.515,-.072);torus(b,charm,p(0,0,1),.029,.006,16,4,7);
                    gem(b,charm.add(p(0,.058,0)),charm.add(p(0,.025,0)),.014,6,7);
                }
            }
        }
    }
}

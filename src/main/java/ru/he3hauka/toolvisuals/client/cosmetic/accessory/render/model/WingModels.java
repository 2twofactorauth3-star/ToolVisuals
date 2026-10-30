package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.*;
import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;

import static ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model.MeshPrimitives.*;

public final class WingModels {
    private WingModels(){}
    public static AccessoryModel build(AccessoryLook.Back type,boolean far){
        var b=new AccessoryMesh.Builder();int split;
        switch(type){
            case NONE -> split=0;
            case ANGEL -> split=angel(b,far);
            case DEMON -> split=demon(b,far);
            case BUTTERFLY -> split=butterfly(b,far);
            case CRYSTAL -> split=crystal(b,far);
            case PHOENIX -> split=OrganicWings.phoenix(b,far);
            case DRAGON -> split=OrganicWings.dragon(b,far);
            case DRAGONFLY -> split=OrganicWings.dragonfly(b,far);
            case MECHANICAL -> split=BackOrnaments.mechanical(b,far);
            case ORBITAL -> split=BackOrnaments.orbital(b,far);
            case SWORDS -> split=BackOrnaments.swords(b,far);
            default -> throw new IllegalArgumentException("Unknown wing model");
        }
        return type==AccessoryLook.Back.ORBITAL
                ?new AccessoryModel(b.build(),split,.46,-.14,.20)
                :new AccessoryModel(b.build(),split,.49,-.32,.10);
    }
    private static int angel(AccessoryMesh.Builder b,boolean far){
        attachment(b,far,6);
        int sides=far?4:6;
        pipe(b,t->bezier(p(.01,.10,.12),p(.21,-.22,.18),p(.42,-.54,.17),p(.88,-.59,.10),t),far?4:9,sides,.026,4);
        int coverts=far?4:8;
        for(int i=0;i<coverts;i++){
            double t=i/(double)(coverts-1);var root=p(.12+.61*t,-.02-.43*t,.18);var tip=p(.33+.63*t,.36-.77*t,.20);
            feather(b,root,tip,.065-.018*t,.026,far?3:5,sides,2+tone(t*.4));
        }
        if(!far){
            for(int i=0;i<6;i++){double t=i/5d;feather(b,p(.04+.36*t,.065-.32*t,.255),p(.22+.38*t,.28-.49*t,.27),.057,.026,4,6,4+tone(t*.25));}
            torus(b,p(.07,.04,.20),p(0,0,1),.060,.012,12,4,7);
            gem(b,p(.07,.10,.225),p(.07,-.035,.225),.035,6,0);
        }
        int split=b.mark(),count=far?6:11;
        for(int i=0;i<count;i++){
            double t=i/(double)(count-1);var root=p(.30+.59*t,-.12-.42*t,.07);var tip=p(.57+.82*t,.70-1.22*t+.05*Math.sin(t*Math.PI),.10+.035*Math.sin(t*Math.PI));
            double width=.069-.014*t;
            feather(b,root,tip,width,.029,far?3:6,far?4:8,tone((1-t)*.45));
            if(!far){
                var direction=tip.subtract(root);var lateral=p(-direction.y,direction.x,0).unit();
                pipe(b,u->mix(root,tip,u).add(lateral.multiply(Math.sin(u*Math.PI)*width*.25)).add(p(0,0,.012+Math.sin(u*Math.PI)*.050)),4,3,.0045,5);
                for(int barb=1;barb<=2;barb++){
                    double at=.28+barb*.20;var middle=mix(root,tip,at).add(p(0,0,.052));
                    for(int side:new int[]{-1,1}){var end=mix(root,tip,at+.13).add(lateral.multiply(side*width*.80)).add(p(0,0,.026));pipe(b,u->mix(middle,end,u),1,3,.0018,6);}
                }
            }
        }
        if(!far)pipe(b,t->bezier(p(.32,-.14,.17),p(.51,-.43,.20),p(.86,-.68,.17),p(1.08,-.55,.12),t),8,5,.017,7);
        return split;
    }
    private static int demon(AccessoryMesh.Builder b,boolean far){
        attachment(b,far,2);
        var joint=p(.34,-.30,.13);
        tube(b,t->bezier(p(.01,.12,.13),p(.13,-.17,.17),p(.19,-.35,.16),joint,t),far?3:6,far?5:8,t->.040-.011*t,0,2);
        torus(b,joint,p(0,0,1),.061,.012,far?8:12,4,6);
        if(!far){gem(b,joint.add(p(0,.06,.05)),joint.add(p(0,-.07,.05)),.035,6,7);}
        int split=b.mark();
        Point[] fingers={p(.78,-.87,.12),p(1.38,-.51,.035),p(1.17,.16,.06),p(.74,.62,.10),p(.27,.37,.11)};
        int radial=far?2:4,angular=far?3:7;
        for(int panel=0;panel<fingers.length-1;panel++){
            var a=fingers[panel];var c=fingers[panel+1];var control=mix(mix(a,c,.5),joint,.33);
            Curve boundary=t->a.multiply((1-t)*(1-t)).add(control.multiply(2*t*(1-t))).add(c.multiply(t*t));
            for(int side:new int[]{-1,1}){
                var points=new Point[radial+1][angular+1];
                for(int r=0;r<=radial;r++)for(int u=0;u<=angular;u++){
                    double radius=r/(double)radial,angle=u/(double)angular;var point=mix(joint,boundary.at(angle),radius);
                    points[r][u]=point.add(p(0,0,.12*Math.sin(radius*Math.PI)*Math.sin(angle*Math.PI)+side*.010));
                }
                grid(b,points,(r,u)->tone(.04+.34*r),side<0);
            }
            pipe(b,t->boundary.at(t).add(p(0,0,.012)),far?3:8,far?3:5,.011,5);
            if(!far){
                for(int vein=1;vein<=2;vein++){
                    double u=vein/3d;
                    pipe(b,t->mix(joint,boundary.at(u),t).add(p(0,0,.014+.12*Math.sin(t*Math.PI)*Math.sin(u*Math.PI))),4,3,.0035,4);
                }
            }
        }
        for(int i=0;i<fingers.length;i++){
            var endpoint=fingers[i];var control=mix(joint,endpoint,.5).add(p(0,-.055,.06));
            tube(b,t->bezier(joint,control,control,endpoint,t),far?3:7,far?4:6,t->.027*(1-t)+.006,1,6);
        }
        tube(b,t->bezier(joint,p(.47,-.64,.18),p(.68,-.80,.16),fingers[0],t),far?3:7,far?5:7,t->.043-.022*t,0,4);
        if(!far){
            for(int i=0;i<4;i++){
                double t=.23+i*.19;var base=bezier(joint,p(.47,-.64,.18),p(.68,-.80,.16),fingers[0],t);
                var tip=base.add(p(.022,-.10-.045*t,-.01));gem(b,base.add(p(0,.025,0)),tip,.025,5,6);
            }
            gem(b,fingers[0].add(p(-.025,.035,0)),fingers[0].add(p(.045,-.12,-.025)),.027,6,7);
            gem(b,fingers[1].add(p(-.035,.01,0)),fingers[1].add(p(.12,-.055,-.02)),.022,5,6);
        }
        return split;
    }
    private static int butterfly(AccessoryMesh.Builder b,boolean far){
        attachment(b,far,1);
        pipe(b,t->bezier(p(.015,.16,.13),p(.11,-.10,.15),p(.14,-.32,.14),p(.22,-.48,.10),t),far?3:6,far?4:6,.027,0);
        if(!far){torus(b,p(.10,.05,.16),p(0,0,1),.045,.008,12,4,7);}
        int split=b.mark();
        petal(b,new Point[]{p(.07,.08,.08),p(.17,-.38,.05),p(.42,-.85,.015),p(.85,-.84,.025),p(1.16,-.49,.015),p(1.05,-.14,.06),p(.66,.10,.11),p(.26,.20,.12)},p(.32,-.18,.13),far,true);
        petal(b,new Point[]{p(.15,.16,.10),p(.53,.12,.08),p(.96,.30,.04),p(.90,.57,.025),p(.60,.77,.045),p(.29,.63,.10),p(.15,.37,.12)},p(.35,.36,.14),far,false);
        return split;
    }
    private static void petal(AccessoryMesh.Builder b,Point[] outline,Point center,boolean far,boolean upper){
        int radial=far?2:4,segments=far?12:28;
        for(int side:new int[]{-1,1}){
            var grid=new Point[radial+1][segments+1];
            for(int r=0;r<=radial;r++)for(int c=0;c<=segments;c++){
                double t=r/(double)radial;var edge=closedSpline(outline,c/(double)segments);
                grid[r][c]=mix(center,edge,t).add(p(0,0,.075*(1-t*t)+side*.008));
            }
            grid(b,grid,(r,u)->tone((mix(center,closedSpline(outline,u),r).y+.9)/1.7),side<0);
        }
        pipe(b,t->closedSpline(outline,t).add(p(0,0,.012)),segments,far?3:5,far?.012:.016,0,.55);
        if(far)return;
        pipe(b,t->mix(center,closedSpline(outline,t),.87).add(p(0,0,.075*(1-.87*.87)+.012)),segments,3,.005,7);
        int veins=upper?6:5;
        for(int i=0;i<veins;i++){
            double angle=(i+.30)/veins;var edge=closedSpline(outline,angle);
            pipe(b,t->mix(center,edge,t).add(p(0,0,.075*(1-t*t)+.018)),4,3,.006,1,.62);
            if(i<veins-1){
                double nextAngle=(i+1.30)/veins;
                for(double ring:new double[]{.42,.67})pipe(b,t->{var end=closedSpline(outline,angle+(nextAngle-angle)*t);double radius=ring+.035*Math.sin(t*Math.PI);return mix(center,end,radius).add(p(0,0,.075*(1-radius*radius)+.019));},3,3,.0035,1,.60);
            }
        }
        for(int i=0;i<9;i++){
            var edge=closedSpline(outline,(i+.35)/9d);var spot=mix(center,edge,.93).add(p(0,0,.045));
            gem(b,spot.add(p(0,.022,0)),spot.add(p(0,-.022,0)),.013,4,7);
        }
        var eye=upper?p(.72,-.45,.19):p(.64,.42,.205);
        double rx=upper?.073:.052,ry=upper?.095:.068;
        pipe(b,t->eye.add(p(Math.cos(t*Math.TAU)*rx,Math.sin(t*Math.TAU)*ry,0)),16,3,.0065,7);
        gem(b,eye.add(p(0,ry*.80,0)),eye.add(p(0,-ry*.80,0)),rx*.71,6,0,.38);
        gem(b,eye.add(p(0,.027,.021)),eye.add(p(0,-.027,.021)),.015,5,7);
    }
    private static int crystal(AccessoryMesh.Builder b,boolean far){
        attachment(b,far,5);
        var root=p(.035,.09,.15);
        gem(b,root.add(p(0,.11,0)),root.add(p(0,-.13,0)),.078,far?5:8,3);
        torus(b,root,p(0,0,1),.080,.012,far?8:16,far?3:5,7);
        if(!far){
            for(int i=0;i<3;i++){double a=(i-.5)*.9;var tip=root.add(p(Math.cos(a)*.18,-Math.sin(a)*.18,.07));gem(b,root,tip,.034,6,5);}
        }
        int split=b.mark(),count=far?4:8;
        for(int i=0;i<count;i++){
            double t=i/(double)(count-1);var base=p(.13+.48*t,.16-.34*t,.10+.06*Math.sin(t*Math.TAU));
            var tip=p(.35+1.02*t,-.88+.96*t,.08+.13*Math.sin(t*Math.PI));
            gem(b,base,tip,.076+.013*Math.sin(t*Math.PI),far?5:7,tone(t*.8));
            pipe(b,u->bezier(root,root.add(p(.16,-.12,.015)),base.add(p(0,.10,0)),base,u),far?2:4,far?3:5,.013,6);
            if(!far){
                torus(b,mix(base,tip,.18),tip.subtract(base),.076+.013*Math.sin(t*Math.PI),.0045,12,3,7);
                var lateral=tip.subtract(base).cross(p(0,0,1)).unit().multiply(.025);
                pipe(b,u->mix(base,tip,.18+u*.69).add(lateral).add(p(0,0,.048)),3,3,.003,7);
            }
        }
        if(!far){
            for(int i=0;i<5;i++){
                double t=i/4d;gem(b,p(.15+.40*t,.14-.23*t,.23),p(.39+.61*t,-.44+.74*t,.32),.047,6,2+tone(t*.55));
            }
            Point[] fragments={p(1.08,-.55,.25),p(1.29,-.25,.25),p(.66,-.78,.21)};
            for(int i=0;i<fragments.length;i++){var point=fragments[i];gem(b,point.add(p(-.025,.06,0)),point.add(p(.025,-.07,0)),.030,6,6);}
        }
        return split;
    }
    private static void attachment(AccessoryMesh.Builder b,boolean far,int color){
        tube(b,t->bezier(p(.005,.10,-.10),p(-.025,.065,-.03),p(.012,.065,.08),p(.025,.10,.14),t),far?2:4,far?5:8,t->.018+.015*Math.sin(t*Math.PI),color,color);
    }
}

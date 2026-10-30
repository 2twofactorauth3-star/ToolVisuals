package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryMesh;

import static ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model.MeshPrimitives.*;

final class HeadAccessories {
    private HeadAccessories() {}

    static void fox(AccessoryMesh.Builder b,boolean far) {
        for(int side:new int[]{-1,1}) {
            double x=side*.205;
            Point[] outline={p(x-side*.11,-.50,-.06),p(x-side*.09,-.70,-.055),p(x+side*.045,-.99,.005),p(x+side*.17,-.72,-.04),p(x+side*.15,-.49,-.05)};
            var center=p(x+side*.026,-.68,-.045);
            shell(b,outline,center,.075,far,(r,u)->r>.76?1:6);
            if(!far) {
                pipe(b,t->mix(center,closedSpline(outline,t),.73).add(p(0,0,-.052)),24,3,.007,7);
                for(int tuft=0;tuft<4;tuft++) {
                    var start=p(x+side*(.09+.015*tuft),-.52-.02*tuft,-.085);
                    feather(b,start,start.add(p(side*.055,-.048-.012*tuft,-.012)),.019,.012,4,5,5+tuft/2);
                }
                gem(b,p(x+side*.10,-.54,-.10),p(x+side*.10,-.62,-.10),.023,6,7);
            }
        }
    }

    static void antlers(AccessoryMesh.Builder b,boolean far) {
        for(int side:new int[]{-1,1}) {
            Curve trunk=t->bezier(p(side*.20,-.48,.045),p(side*.42,-.66,.10),p(side*.60,-1.03,.025),p(side*.44,-1.22,-.03),t);
            tube(b,trunk,far?5:14,far?4:7,t->.045*Math.pow(1-t,.8)+.003,1,6);
            torus(b,trunk.at(0),trunk.at(.02).subtract(trunk.at(0)),.049,.010,far?8:16,far?3:5,4);
            int branches=far?2:5;
            for(int i=0;i<branches;i++) {
                double at=.24+i*(far?.30:.12);var start=trunk.at(at);
                var tip=start.add(p(side*(i%2==0?.18:-.11),-.23-.025*i,i%2==0?.035:-.09));
                double thickness=.027-.002*i;
                tube(b,t->bezier(start,start.add(p(side*.10,-.04,0)),tip.add(p(0,.09,0)),tip,t),far?3:7,far?4:6,t->thickness*(1-t)+.001,2,7);
                if(!far)gem(b,tip.add(p(0,.025,0)),tip.add(p(0,-.025,0)),.012,5,7);
            }
            if(!far) {
                var start=trunk.at(.37).add(p(0,0,-.035));
                pipe(b,t->bezier(start,start.add(p(side*.04,.08,0)),start.add(p(side*.10,.12,0)),start.add(p(side*.08,.18,0)),t),8,3,.006,7);
                gem(b,start.add(p(side*.08,.18,0)),start.add(p(side*.08,.28,0)),.025,6,6);
            }
        }
    }

    static void witch(AccessoryMesh.Builder b,boolean far) {
        var center=p(0,-.525,0);
        torus(b,center,p(.03,1,-.03),.37,.075,far?12:40,far?4:8,1);
        Curve cone=t->bezier(p(0,-.54,0),p(-.035,-.80,0),p(.05,-1.28,.04),p(.31,-1.13,.08),t);
        tube(b,cone,far?6:22,far?8:16,t->.265*Math.pow(1-t,1.1)+.002,1,4);
        torus(b,p(0,-.60,.002),p(0,1,0),.239,.022,far?12:32,far?3:5,6);
        gem(b,p(0,-.57,-.25),p(0,-.68,-.25),.050,far?4:8,7);
        if(!far) {
            torus(b,center.add(p(0,-.025,0)),p(.03,1,-.03),.424,.006,40,3,7);
            for(int i=0;i<5;i++) {
                double t=.16+i*.11;
                var star=cone.at(t).add(p(.045*Math.sin(i*2),0,-(.265*Math.pow(1-t,1.1)+.006)));
                gem(b,star.add(p(0,.021,0)),star.add(p(0,-.021,0)),.014,4,7);
                gem(b,star.add(p(-.023,0,0)),star.add(p(.023,0,0)),.008,4,7);
            }
        }
    }

    static void headphones(AccessoryMesh.Builder b,boolean far) {
        Curve band=t->p(-Math.cos(t*Math.PI)*.303,-.26-Math.sin(t*Math.PI)*.33,.025);
        pipe(b,band,far?6:24,far?4:7,.030,1);
        if(!far)pipe(b,t->band.at(t).add(p(0,-.018,.024)),24,4,.008,7);
        for(int side:new int[]{-1,1}) {
            var center=p(side*.285,-.255,.015);
            torus(b,center,p(1,0,0),.115,.039,far?10:28,far?4:7,0);
            gem(b,center.add(p(-side*.012,0,0)),center.add(p(side*.096,0,0)),.111,far?6:12,3);
            if(!far) {
                var outside=center.add(p(side*.086,0,0));
                torus(b,outside,p(1,0,0),.082,.009,24,4,7);
                for(int bar=0;bar<4;bar++) {
                    double z=(bar-1.5)*.030,height=.028+.010*(bar%3);
                    pipe(b,t->outside.add(p(side*.006,-height+height*2*t,z)),1,4,.006,6);
                }
            }
        }
        if(!far) {
            Curve microphone=t->bezier(p(-.32,-.23,-.025),p(-.39,-.07,-.18),p(-.24,.015,-.34),p(-.12,.005,-.35),t);
            pipe(b,microphone,12,5,.010,2);
            gem(b,microphone.at(1).add(p(-.025,0,0)),microphone.at(1).add(p(.025,0,0)),.020,6,1);
        }
    }

    static void flowers(AccessoryMesh.Builder b,boolean far) {
        torus(b,p(0,-.535,0),p(0,1,0),.288,.016,far?12:36,far?3:5,1);
        if(!far)pipe(b,t->p(Math.cos(t*Math.TAU)*.29,-.535+.018*Math.sin(t*Math.TAU*5),Math.sin(t*Math.TAU)*.29),48,3,.006,6);
        int count=far?3:7,petals=far?3:6;
        for(int i=0;i<count;i++) {
            double angle=-Math.PI/2+(i-(count-1)/2d)*(far?.90:.47);
            var outward=p(Math.cos(angle),0,Math.sin(angle));var across=p(-Math.sin(angle),0,Math.cos(angle));
            var center=p(outward.x*.298,-.57,outward.z*.298);
            for(int petal=0;petal<petals;petal++) {
                double a=petal*Math.TAU/petals;var radial=across.multiply(Math.cos(a)).add(p(0,Math.sin(a),0));
                feather(b,center,center.add(radial.multiply(.075)).add(outward.multiply(.021)),.024,.014,far?2:4,far?4:6,3+petal%4);
            }
            gem(b,center.subtract(outward.multiply(.012)),center.add(outward.multiply(.038)),.027,far?4:7,7);
            if(!far) {
                var leaf=center.add(across.multiply(.08));
                feather(b,leaf,leaf.add(across.multiply(.075)).add(p(0,.04,0)),.024,.012,4,6,1);
            }
        }
    }

    static int astrolabe(AccessoryMesh.Builder b,boolean far) {
        var center=p(0,-.88,0);
        gem(b,center.add(p(0,.09,0)),center.add(p(0,-.09,0)),.055,far?5:9,6);
        if(!far)torus(b,center,p(0,1,0),.105,.008,20,4,7);
        int split=b.mark(),count=far?2:3;
        for(int i=0;i<count;i++) {
            var axis=i==0?p(0,1,0):i==1?p(.8,.3,.15):p(-.5,.35,.8);
            double radius=.21+i*.075;
            torus(b,center,axis,radius,.010,far?12:32,far?3:5,2+i*2);
        }
        if(!far)for(int i=0;i<10;i++) {
            double a=i*Math.TAU/10;var star=center.add(p(Math.cos(a)*.36,.025,Math.sin(a)*.36));
            gem(b,star.add(p(0,.024,0)),star.add(p(0,-.024,0)),.018,5,7);
        }
        return split;
    }
}

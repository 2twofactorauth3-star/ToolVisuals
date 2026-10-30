package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryMesh;

import static ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model.MeshPrimitives.*;

final class BackOrnaments {
    private BackOrnaments() {}

    static int mechanical(AccessoryMesh.Builder b,boolean far) {
        var root=p(.02,.08,.14);var hinge=p(.37,-.25,.18);
        pipe(b,t->mix(root,hinge,t),far?2:4,far?4:8,.045,1);
        torus(b,hinge,p(0,0,1),.088,.023,far?10:24,far?3:6,4);
        gem(b,hinge.add(p(0,.043,.025)),hinge.add(p(0,-.043,.025)),.032,far?4:8,7);
        if(!far) {
            for(int i=0;i<8;i++) {
                double a=i*Math.TAU/8;var tooth=hinge.add(p(Math.cos(a)*.112,Math.sin(a)*.112,0));
                gem(b,tooth,tooth.add(p(Math.cos(a)*.055,Math.sin(a)*.055,0)),.022,4,3);
            }
            pipe(b,t->mix(root.add(p(0,.08,.07)),hinge.add(p(.04,.08,.07)),t),4,5,.014,6);
        }
        int split=b.mark(),count=far?4:7;
        for(int i=0;i<count;i++) {
            double t=i/(double)(count-1);
            var base=p(.36+.22*t,-.24-.08*t,.13);
            var tip=p(.58+.83*t,.76-1.29*t,.11+.04*t);
            gem(b,base,tip,.046,far?4:6,tone(.08+.50*t));
            if(!far) {
                var inset=mix(base,tip,.16).add(p(0,0,.036));
                pipe(b,u->mix(inset,tip.add(p(0,0,.024)),u*.80),4,4,.008,7);
                var brace=mix(base,tip,.38);
                torus(b,brace,tip.subtract(base),.037,.006,12,3,6);
            }
        }
        if(!far) {
            pipe(b,t->bezier(hinge,p(.54,-.54,.22),p(.94,-.62,.22),p(1.36,-.48,.14),t),8,6,.027,2);
            pipe(b,t->bezier(hinge.add(p(0,.03,.06)),p(.51,-.43,.29),p(.79,-.41,.28),p(1.02,-.24,.21),t),8,4,.010,7);
        }
        return split;
    }

    static int orbital(AccessoryMesh.Builder b,boolean far) {
        var center=p(.46,-.14,.20);
        pipe(b,t->bezier(p(.01,.08,.14),p(.12,.10,.20),p(.24,-.05,.23),center,t),far?3:7,far?4:6,.021,2);
        gem(b,center.add(p(0,.16,0)),center.add(p(0,-.16,0)),.080,far?5:9,5);
        if(!far)torus(b,center,p(0,0,1),.12,.009,24,4,7);
        int split=b.mark(),rings=far?2:3;
        for(int i=0;i<rings;i++) {
            double angle=i*1.05;
            var axis=p(Math.sin(angle)*.36,Math.cos(angle)*.48,1);
            double radius=.30+i*.14;
            torus(b,center,axis,radius,far?.011:.014,far?12:36,far?3:5,2+i*2);
            if(!far) {
                double x=Math.cos(angle)*radius,y=Math.sin(angle)*radius;
                var star=center.add(p(x,y,-(axis.x*x+axis.y*y)));
                gem(b,star.add(p(0,.042,0)),star.add(p(0,-.042,0)),.028,6,7);
                for(int tick=0;tick<8;tick++) {
                    double a=tick*Math.TAU/8,dx=Math.cos(a),dy=Math.sin(a);
                    var start=center.add(p(dx*(radius-.02),dy*(radius-.02),-(axis.x*dx+axis.y*dy)*(radius-.02)));
                    var end=center.add(p(dx*(radius+.025),dy*(radius+.025),-(axis.x*dx+axis.y*dy)*(radius+.025)));
                    pipe(b,t->mix(start,end,t),1,3,.004,7);
                }
            }
        }
        return split;
    }

    static int swords(AccessoryMesh.Builder b,boolean far) {
        var bottom=p(.02,.88,.15);var guard=p(.47,-.43,.19);var tip=p(.64,-.89,.19);
        pipe(b,t->mix(p(.02,.38,.03),p(.38,-.12,.16),t),far?2:6,far?4:7,.028,1);
        gem(b,bottom,guard,.065,far?4:8,2);
        var direction=tip.subtract(guard).unit();var across=p(-direction.y,direction.x,0);
        pipe(b,t->mix(guard.subtract(across.multiply(.18)),guard.add(across.multiply(.18)),t),far?1:5,far?4:7,.027,5);
        tube(b,t->mix(guard,tip,t),far?2:6,far?4:7,t->.032*(1-.20*t),1,3);
        gem(b,tip.add(p(0,.03,0)),tip.add(p(0,-.07,0)),.042,far?4:8,7);
        int split=b.mark();
        if(far) {
            gem(b,guard.add(p(0,.04,.04)),guard.add(p(0,-.04,.04)),.027,4,7);
        } else {
            for(int i=0;i<6;i++)torus(b,mix(guard,tip,(i+1)/8d),direction,.031,.005,12,3,i%2==0?6:0);
            pipe(b,t->mix(bottom,guard,t).add(p(0,0,.042*Math.sin(t*Math.PI))),8,3,.006,7);
            for(int side:new int[]{-1,1}) {
                var end=guard.add(across.multiply(side*.19));
                gem(b,end.add(p(0,.035,0)),end.add(p(0,-.045,0)),.027,6,7);
            }
            var charm=guard.add(p(-.13,.05,.05));
            pipe(b,t->bezier(charm,charm.add(p(-.08,.10,0)),charm.add(p(.02,.18,.04)),charm.add(p(-.03,.24,.02)),t),6,4,.007,6);
            gem(b,charm.add(p(-.03,.24,.02)),charm.add(p(-.03,.34,.02)),.026,6,7);
        }
        return split;
    }
}

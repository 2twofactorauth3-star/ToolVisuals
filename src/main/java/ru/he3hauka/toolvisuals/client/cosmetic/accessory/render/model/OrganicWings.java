package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryMesh;

import static ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model.MeshPrimitives.*;

final class OrganicWings {
    private OrganicWings() {}

    static int phoenix(AccessoryMesh.Builder b,boolean far) {
        var root=p(.015,.08,.12);
        pipe(b,t->bezier(root,p(.14,-.35,.22),p(.47,-.66,.20),p(.88,-.62,.14),t),far?4:10,far?4:7,.033,2);
        gem(b,root.add(p(0,.09,.04)),root.add(p(.08,-.19,.04)),.060,far?5:8,6);
        if(!far)torus(b,root,p(0,0,1),.074,.010,16,4,7);
        int split=b.mark(),feathers=far?5:10;
        for(int i=0;i<feathers;i++) {
            double t=i/(double)(feathers-1);
            var start=p(.18+.72*t,-.03-.57*t,.10);
            var end=p(.39+1.03*t,.76-1.60*t,.17+.10*Math.sin(t*Math.PI));
            feather(b,start,end,.062-.018*t,.034,far?3:7,far?4:7,tone(t*.65));
            if(!far)pipe(b,u->mix(start,end,u).add(p(0,0,.042*Math.sin(u*Math.PI))),5,3,.004,7);
        }
        if(!far) {
            for(int i=0;i<7;i++) {
                double t=i/6d;
                feather(b,p(.08+.60*t,-.05-.40*t,.24),p(.32+.74*t,.26-.92*t,.29),.051,.029,5,6,3+tone(t*.45));
            }
            for(int i=0;i<3;i++) {
                double x=.20+i*.15,tipHeight=.94-i*.19;
                Curve plume=t->bezier(p(x,.11,.17),p(x+.23,.52,.30),p(x+.36,1.09,.14),p(x+.70,tipHeight,.22),t);
                tube(b,plume,12,5,t->.033*Math.pow(Math.sin(Math.PI*t),.7)+.001,3,7);
                pipe(b,t->plume.at(t).add(p(0,0,.027)),10,3,.0035,7);
            }
        }
        return split;
    }

    static int dragon(AccessoryMesh.Builder b,boolean far) {
        var joint=p(.30,-.24,.17);
        tube(b,t->bezier(p(.01,.12,.12),p(.12,-.05,.19),p(.12,-.35,.21),joint,t),far?3:8,far?4:7,t->.044-.010*t,0,4);
        gem(b,joint.add(p(0,.08,.035)),joint.add(p(.02,-.11,.035)),.064,far?5:8,6);
        int split=b.mark();
        Point[] tips={p(.77,-1.01,.17),p(1.38,-.54,.12),p(1.26,.07,.14),p(.86,.51,.18),p(.36,.41,.18)};
        for(int i=0;i<tips.length-1;i++) {
            var a=tips[i];var c=tips[i+1];
            var scallop=mix(joint,mix(a,c,.5),.57);
            var outline=new Point[]{joint,a,mix(a,scallop,.60),scallop,mix(c,scallop,.60),c};
            var center=mix(joint,mix(a,c,.5),.50);
            shell(b,outline,center,.048,far,(r,u)->tone(.14+.54*r));
            if(!far)for(int vein=1;vein<=2;vein++) {
                double at=vein/3d;
                pipe(b,t->mix(joint,mix(a,c,at),t*.78).add(p(0,0,.049*Math.sin(t*Math.PI))),5,3,.0045,6);
            }
        }
        for(int i=0;i<tips.length;i++) {
            var tip=tips[i];
            tube(b,t->bezier(joint,mix(joint,tip,.35).add(p(.015,-.05,.04)),mix(joint,tip,.7),tip,t),far?3:8,far?4:6,t->.028*(1-t)+.008,1,5);
            if(!far)gem(b,tip.add(p(-.025,.022,0)),tip.add(p(.043,-.09,-.015)),.020,6,7);
        }
        if(!far)for(int i=0;i<6;i++) {
            double t=i/5d;
            var scale=p(.11+.44*t,-.02-.50*t,.26);
            gem(b,scale.add(p(-.025,.035,0)),scale.add(p(.040,-.07,0)),.035,5,3+i/2);
        }
        return split;
    }

    static int dragonfly(AccessoryMesh.Builder b,boolean far) {
        var root=p(.025,.08,.15);
        pipe(b,t->mix(root,p(.27,-.18,.18),t),far?2:5,far?4:7,.023,4);
        if(!far)torus(b,root,p(0,0,1),.047,.010,16,4,7);
        int split=b.mark();
        for(int lobe=0;lobe<2;lobe++) {
            boolean upper=lobe==0;
            var center=upper?p(.52,-.31,.18):p(.52,.25,.21);
            Point[] outline=upper
                    ?new Point[]{p(.07,.05,.14),p(.35,-.35,.15),p(1.17,-.77,.06),p(1.45,-.68,.04),p(1.31,-.41,.09),p(.62,-.04,.19)}
                    :new Point[]{p(.10,.10,.17),p(.64,.04,.21),p(1.25,.25,.12),p(1.27,.48,.10),p(.99,.59,.12),p(.38,.34,.23)};
            shell(b,outline,center,.042,far,(r,u)->tone(.30+.58*r));
            if(!far) {
                for(int vein=0;vein<6;vein++) {
                    double at=(vein+.25)/6;
                    var edge=closedSpline(outline,at);
                    pipe(b,t->mix(center,edge,t).add(p(0,0,.043*(1-t*t))),4,3,.004,1);
                }
                pipe(b,t->mix(center,closedSpline(outline,t),.70).add(p(0,0,.030)),24,3,.003,6);
                for(int i=0;i<5;i++) {
                    var bead=mix(center,closedSpline(outline,(i+.6)/5),.88).add(p(0,0,.026));
                    gem(b,bead.add(p(0,.017,0)),bead.add(p(0,-.017,0)),.009,4,7);
                }
            }
        }
        return split;
    }
}

package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model;

import java.util.function.DoubleUnaryOperator;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryMesh;

final class MeshPrimitives {
    static final class Point {
        public final double x,y,z;
        Point(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
        Point add(Point b){return p(x+b.x,y+b.y,z+b.z);}
        Point subtract(Point b){return p(x-b.x,y-b.y,z-b.z);}
        Point multiply(double amount){return p(x*amount,y*amount,z*amount);}
        Point cross(Point b){return p(y*b.z-z*b.y,z*b.x-x*b.z,x*b.y-y*b.x);}
        double lengthSquared(){return x*x+y*y+z*z;}
        Point unit(){double length=Math.sqrt(lengthSquared());return length<1e-9?p(0,0,1):multiply(1/length);}
    }
    @FunctionalInterface interface Curve {Point at(double t);}
    @FunctionalInterface interface Tone {int at(double u,double v);}
    private static final Point LIGHT=p(-.32,-.65,.69).unit();
    private MeshPrimitives(){}
    static Point p(double x,double y,double z){return new Point(x,y,z);}
    static Point mix(Point a,Point b,double t){return a.multiply(1-t).add(b.multiply(t));}
    static Point bezier(Point a,Point b,Point c,Point d,double t){double n=1-t;return a.multiply(n*n*n).add(b.multiply(3*n*n*t)).add(c.multiply(3*n*t*t)).add(d.multiply(t*t*t));}
    static int tone(double t){return (int)Math.clamp(Math.round(t*7),0,7);}
    static double light(Point normal){var n=normal.unit();return .46+.54*Math.clamp(n.x*LIGHT.x+n.y*LIGHT.y+n.z*LIGHT.z+.08,0,1);}
    static void vertex(AccessoryMesh.Builder b,Point p,int tone,double shade){b.vertex(p.x,p.y,p.z,tone,Math.clamp(shade,0,1));}
    static void face(AccessoryMesh.Builder b,Point a,Point c,Point d,Point e,int tone){face(b,a,c,d,e,tone,1);}
    static void face(AccessoryMesh.Builder b,Point a,Point c,Point d,Point e,int tone,double intensity){
        var normal=c.subtract(a).cross(d.subtract(a));if(normal.lengthSquared()<1e-12)normal=d.subtract(a).cross(e.subtract(a));
        double shade=light(normal)*intensity;vertex(b,a,tone,shade);vertex(b,c,tone,shade);vertex(b,d,tone,shade);vertex(b,e,tone,shade);
    }
    static Point[] curve(Curve shape,int segments){var points=new Point[segments+1];for(int i=0;i<=segments;i++)points[i]=shape.at(i/(double)segments);return points;}
    static void tube(AccessoryMesh.Builder b,Curve path,int segments,int sides,DoubleUnaryOperator radius,int startTone,int endTone){
        tube(b,path,segments,sides,radius,startTone,endTone,1);
    }
    private static void tube(AccessoryMesh.Builder b,Curve path,int segments,int sides,DoubleUnaryOperator radius,int startTone,int endTone,double intensity){
        var centers=curve(path,segments);var rings=new Point[segments+1][sides];var normals=new Point[segments+1][sides];
        for(int i=0;i<=segments;i++){
            var direction=centers[Math.min(segments,i+1)].subtract(centers[Math.max(0,i-1)]).unit();
            var ref=Math.abs(direction.z)<.9?p(0,0,1):p(0,1,0);var x=direction.cross(ref).unit();var y=direction.cross(x).unit();
            double r=radius.applyAsDouble(i/(double)segments);
            for(int j=0;j<sides;j++){double a=j*Math.TAU/sides;var normal=x.multiply(Math.cos(a)).add(y.multiply(Math.sin(a)));normals[i][j]=normal;rings[i][j]=centers[i].add(normal.multiply(r));}
        }
        for(int i=0;i<segments;i++)for(int j=0;j<sides;j++){
            int next=(j+1)%sides;int color=(int)Math.round(startTone+(endTone-startTone)*(i+.5)/segments);
            vertex(b,rings[i][j],color,light(normals[i][j])*intensity);vertex(b,rings[i][next],color,light(normals[i][next])*intensity);vertex(b,rings[i+1][next],color,light(normals[i+1][next])*intensity);vertex(b,rings[i+1][j],color,light(normals[i+1][j])*intensity);
        }
    }
    static void pipe(AccessoryMesh.Builder b,Curve path,int segments,int sides,double radius,int color){tube(b,path,segments,sides,t->radius,color,color);}
    static void pipe(AccessoryMesh.Builder b,Curve path,int segments,int sides,double radius,int color,double intensity){tube(b,path,segments,sides,t->radius,color,color,intensity);}
    static void torus(AccessoryMesh.Builder b,Point center,Point axis,double radius,double width,int segments,int sides,int color){
        var n=axis.unit();var ref=Math.abs(n.z)<.9?p(0,0,1):p(0,1,0);var x=n.cross(ref).unit();var y=n.cross(x).unit();
        pipe(b,t->center.add(x.multiply(Math.cos(t*Math.TAU)*radius)).add(y.multiply(Math.sin(t*Math.TAU)*radius)),segments,sides,width,color);
    }
    static void gem(AccessoryMesh.Builder b,Point base,Point tip,double width,int sides,int color){
        gem(b,base,tip,width,sides,color,1);
    }
    static void gem(AccessoryMesh.Builder b,Point base,Point tip,double width,int sides,int color,double intensity){
        var tangent=tip.subtract(base).unit();var ref=Math.abs(tangent.z)<.9?p(0,0,1):p(0,1,0);var x=tangent.cross(ref).unit();var y=tangent.cross(x).unit();
        double[] levels={0,.18,.68,1},radii={0,width,width*.69,0};var rings=new Point[4][sides];
        for(int i=0;i<4;i++)for(int j=0;j<sides;j++){double angle=j*Math.TAU/sides;var center=mix(base,tip,levels[i]);rings[i][j]=center.add(x.multiply(Math.cos(angle)*radii[i])).add(y.multiply(Math.sin(angle)*radii[i]));}
        for(int i=0;i<3;i++)for(int j=0;j<sides;j++){int next=(j+1)%sides;face(b,rings[i][j],rings[i][next],rings[i+1][next],rings[i+1][j],Math.clamp(color+(j%3==0?1:0),0,7),intensity);}
    }
    static void grid(AccessoryMesh.Builder b,Point[][] points,Tone tones,boolean back){
        int rows=points.length-1,columns=points[0].length-1;
        var normals=new Point[rows+1][columns+1];
        for(int r=0;r<=rows;r++)for(int c=0;c<=columns;c++){
            var u=points[r][Math.min(columns,c+1)].subtract(points[r][Math.max(0,c-1)]);
            var v=points[Math.min(rows,r+1)][c].subtract(points[Math.max(0,r-1)][c]);
            var n=u.cross(v).unit();if((n.z<0)!=back)n=n.multiply(-1);normals[r][c]=n;
        }
        for(int r=0;r<rows;r++)for(int c=0;c<columns;c++){
            int[] rr={r,r,r+1,r+1},cc={c,c+1,c+1,c};
            for(int i=0;i<4;i++){int row=rr[i],col=cc[i];vertex(b,points[row][col],tones.at(row/(double)rows,col/(double)columns),light(normals[row][col]));}
        }
    }
    static Point closedSpline(Point[] outline,double position){
        double value=position*outline.length;int i=(int)Math.floor(value);double t=value-i;int count=outline.length;
        var a=outline[Math.floorMod(i-1,count)];var c=outline[Math.floorMod(i,count)];var d=outline[Math.floorMod(i+1,count)];var e=outline[Math.floorMod(i+2,count)];
        return c.multiply(2).add(d.subtract(a).multiply(t)).add(a.multiply(2).subtract(c.multiply(5)).add(d.multiply(4)).subtract(e).multiply(t*t)).add(a.multiply(-1).add(c.multiply(3)).subtract(d.multiply(3)).add(e).multiply(t*t*t)).multiply(.5);
    }
    static void feather(AccessoryMesh.Builder b,Point root,Point tip,double width,double depth,int segments,int sides,int color){
        var direction=tip.subtract(root);var lateral=p(-direction.y,direction.x,0).unit();
        var rings=new Point[segments+1][sides];var normals=new Point[segments+1][sides];
        for(int i=0;i<=segments;i++){
            double t=i/(double)segments,shape=Math.pow(Math.sin(Math.PI*t),.68);
            var center=mix(root,tip,t).add(lateral.multiply(Math.sin(Math.PI*t)*width*.25)).add(p(0,0,Math.sin(Math.PI*t)*depth*.65));
            for(int j=0;j<sides;j++){
                double angle=j*Math.TAU/sides;var normal=lateral.multiply(Math.cos(angle)).add(p(0,0,Math.sin(angle))).unit();
                normals[i][j]=normal;rings[i][j]=center.add(lateral.multiply(Math.cos(angle)*width*shape)).add(p(0,0,Math.sin(angle)*depth*shape));
            }
        }
        for(int i=0;i<segments;i++)for(int j=0;j<sides;j++){
            int next=(j+1)%sides;int[] rr={i,i,i+1,i+1},cc={j,next,next,j};
            for(int k=0;k<4;k++){int row=rr[k],col=cc[k];vertex(b,rings[row][col],Math.clamp(color+(row==0?1:0),0,7),light(normals[row][col]));}
        }
    }
    static void shell(AccessoryMesh.Builder b,Point[] outline,Point center,double depth,boolean far,Tone colors){
        int rows=far?1:3,segments=far?10:24;
        for(int side:new int[]{-1,1}){
            var points=new Point[rows+1][segments+1];
            for(int row=0;row<=rows;row++)for(int column=0;column<=segments;column++){
                double radius=row/(double)rows;
                points[row][column]=mix(center,closedSpline(outline,column/(double)segments),radius)
                        .add(p(0,0,side*depth*(1-radius*radius)));
            }
            grid(b,points,colors,side<0);
        }
        pipe(b,t->closedSpline(outline,t),segments,far?3:5,far?.009:.013,1);
    }
}

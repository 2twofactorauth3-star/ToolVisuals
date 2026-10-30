package ru.he3hauka.toolvisuals.client.hand.editor;

import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2d;
import org.joml.Vector4f;

public final class HandProjection {
    public record Clip(double x,double y,double w) {}
    public record Plane(Clip anchor,Clip xAxis,Clip yAxis,int width,int height) {
        public Vector2d movement(double dx,double dy) {
            double x=anchor.x()/anchor.w()+2*dx/width,y=anchor.y()/anchor.w()-2*dy/height;
            double a=xAxis.x()-x*xAxis.w(),b=yAxis.x()-x*yAxis.w();
            double c=xAxis.y()-y*xAxis.w(),d=yAxis.y()-y*yAxis.w();
            double e=x*anchor.w()-anchor.x(),f=y*anchor.w()-anchor.y(),det=a*d-b*c;
            if(!Double.isFinite(det)||Math.abs(det)<1e-9)return new Vector2d();
            double mx=(e*d-b*f)/det,my=(a*f-e*c)/det;
            return Double.isFinite(mx)&&Double.isFinite(my)?new Vector2d(mx,my):new Vector2d();
        }
    }
    public record Frame(float x,float y,float width,float height,Plane plane) {
        public boolean contains(double px,double py){return px>=x&&px<=x+width&&py>=y&&py<=y+height;}
        public double distance(double px,double py){return Math.hypot((px-x-width/2)/Math.max(8,width),(py-y-height/2)/Math.max(8,height));}
    }
    private static final float NEAR=.05001f;
    private HandProjection() {}

    public static Frame project(AABB box,Matrix4fc model,Matrix4fc translationBasis,Matrix4fc view,float fov,int width,int height) {
        if(width<1||height<1||!Float.isFinite(fov)||fov<1||fov>179||!model.isFinite()||!translationBasis.isFinite()||!view.isFinite())return null;
        var viewProjection=new Matrix4f().setPerspective((float)Math.toRadians(fov),(float)width/height,.05f,4096).mul(view);
        var transform=new Matrix4f(viewProjection).mul(model);
        var corners=new Vector4f[8];
        double minX=Double.POSITIVE_INFINITY,minY=minX,maxX=Double.NEGATIVE_INFINITY,maxY=maxX;
        Vector4f visible=null;
        for(int index=0;index<8;index++)corners[index]=new Vector4f((float)((index&1)==0?box.minX:box.maxX),
                (float)((index&2)==0?box.minY:box.maxY),(float)((index&4)==0?box.minZ:box.maxZ),1).mul(transform);
        var points=new java.util.ArrayList<Vector4f>(20);
        for(int index=0;index<8;index++) {
            var point=corners[index];if(point.w>=NEAR)points.add(point);
            for(int bit=1;bit<=4;bit<<=1)if((index&bit)==0) {
                var other=corners[index|bit];
                if((point.w>=NEAR)!=(other.w>=NEAR))points.add(new Vector4f(point).lerp(other,(NEAR-point.w)/(other.w-point.w)));
            }
        }
        for(var point:points) {
            if(!point.isFinite()||point.w<NEAR*.99f)continue;
            double px=(point.x/point.w*.5+.5)*width,py=(.5-point.y/point.w*.5)*height;
            if(!Double.isFinite(px)||!Double.isFinite(py))continue;
            minX=Math.min(minX,px);maxX=Math.max(maxX,px);minY=Math.min(minY,py);maxY=Math.max(maxY,py);visible=point;
        }
        if(visible==null||maxX<0||maxY<0||minX>width||minY>height)return null;
        float left=(float)Math.clamp(minX-5,0,width),right=(float)Math.clamp(maxX+5,0,width);
        float top=(float)Math.clamp(minY-5,0,height),bottom=(float)Math.clamp(maxY+5,0,height);
        if(right-left<2||bottom-top<2)return null;
        var center=new Vector4f((float)((box.minX+box.maxX)/2),(float)((box.minY+box.maxY)/2),(float)((box.minZ+box.maxZ)/2),1).mul(transform);
        if(!center.isFinite()||center.w<NEAR)center=visible;
        var basis=new Matrix4f(viewProjection).mul(translationBasis);
        var x=new Vector4f(1,0,0,0).mul(basis);var y=new Vector4f(0,1,0,0).mul(basis);
        return new Frame(left,top,right-left,bottom-top,new Plane(clip(center),clip(x),clip(y),width,height));
    }

    private static Clip clip(Vector4f value){return new Clip(value.x,value.y,value.w);}
}

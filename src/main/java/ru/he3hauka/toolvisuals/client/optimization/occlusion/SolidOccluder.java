package ru.he3hauka.toolvisuals.client.optimization.occlusion;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SolidOccluder {
    @FunctionalInterface public interface Cells {boolean solid(int x,int y,int z);}
    private SolidOccluder(){}
    public static AABB find(Vec3 origin,AABB target,Cells cells,int steps){
        var end=target.getCenter();double dx=end.x-origin.x,dy=end.y-origin.y,dz=end.z-origin.z;
        int x=floor(origin.x),y=floor(origin.y),z=floor(origin.z),sx=sign(dx),sy=sign(dy),sz=sign(dz);
        if(cells.solid(x,y,z))return null;
        double tx=boundary(origin.x,dx,x,sx),ty=boundary(origin.y,dy,y,sy),tz=boundary(origin.z,dz,z,sz),ix=dx==0?Double.POSITIVE_INFINITY:Math.abs(1/dx),iy=dy==0?Double.POSITIVE_INFINITY:Math.abs(1/dy),iz=dz==0?Double.POSITIVE_INFINITY:Math.abs(1/dz);
        for(int step=0;step<steps;step++){
            double next=Math.min(tx,Math.min(ty,tz));if(next>=1||!Double.isFinite(next))return null;
            if(tx<=next){x+=sx;tx+=ix;}if(ty<=next){y+=sy;ty+=iy;}if(tz<=next){z+=sz;tz+=iz;}
            if(cells.solid(x,y,z)){
                var box=expand(x,y,z,cells);return covers(origin,target,box)?box:null;
            }
        }
        return null;
    }
    private static AABB expand(int x,int y,int z,Cells cells){
        int minX=x,minY=y,minZ=z,maxX=x,maxY=y,maxZ=z;
        for(int pass=0;pass<3;pass++)for(int side=0;side<6;side++){
            int ax=minX,ay=minY,az=minZ,bx=maxX,by=maxY,bz=maxZ;
            switch(side){case 0->ax=bx=minX-1;case 1->ax=bx=maxX+1;case 2->ay=by=minY-1;case 3->ay=by=maxY+1;case 4->az=bz=minZ-1;default->az=bz=maxZ+1;}
            boolean solid=true;for(int px=ax;solid&&px<=bx;px++)for(int py=ay;solid&&py<=by;py++)for(int pz=az;pz<=bz;pz++)if(!cells.solid(px,py,pz)){solid=false;break;}
            if(solid)switch(side){case 0->minX--;case 1->maxX++;case 2->minY--;case 3->maxY++;case 4->minZ--;default->maxZ++;}
        }
        return new AABB(minX,minY,minZ,maxX+1,maxY+1,maxZ+1);
    }
    public static boolean covers(Vec3 eye,AABB target,AABB solid){
        if(solid==null||solid.contains(eye)||solid.intersects(target))return false;
        for(int point=0;point<8;point++)if(!intersects(eye,(point&1)==0?target.minX:target.maxX,(point&2)==0?target.minY:target.maxY,(point&4)==0?target.minZ:target.maxZ,solid))return false;
        return true;
    }
    private static boolean intersects(Vec3 eye,double x,double y,double z,AABB box){
        double low=0,high=1,dx=x-eye.x,dy=y-eye.y,dz=z-eye.z;
        for(int axis=0;axis<3;axis++){
            double start=axis==0?eye.x:axis==1?eye.y:eye.z,delta=axis==0?dx:axis==1?dy:dz,min=axis==0?box.minX:axis==1?box.minY:box.minZ,max=axis==0?box.maxX:axis==1?box.maxY:box.maxZ;
            if(Math.abs(delta)<1e-12){if(start<=min||start>=max)return false;continue;}
            double a=(min-start)/delta,b=(max-start)/delta;low=Math.max(low,Math.min(a,b));high=Math.min(high,Math.max(a,b));if(high-low<1e-7)return false;
        }
        return low>1e-7&&low<1-1e-7&&high>low;
    }
    private static double boundary(double value,double delta,int cell,int sign){return delta==0?Double.POSITIVE_INFINITY:((sign>0?cell+1:cell)-value)/delta;}
    private static int floor(double value){return (int)Math.floor(value);}
    private static int sign(double value){return value>0?1:value<0?-1:0;}
}

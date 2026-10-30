package ru.he3hauka.toolvisuals.client.optimization.terrain;

public final class BoundaryFaces {
    private BoundaryFaces() {}

    public static int mask(float x0,float y0,float z0,float x1,float y1,float z1,
                           float x2,float y2,float z2,float x3,float y3,float z3) {
        if(!inside(x0)||!inside(y0)||!inside(z0)||!inside(x1)||!inside(y1)||!inside(z1)
                ||!inside(x2)||!inside(y2)||!inside(z2)||!inside(x3)||!inside(y3)||!inside(z3))return 0;
        int result=0;
        if(y0==0&&y1==0&&y2==0&&y3==0)result|=1;
        if(y0==1&&y1==1&&y2==1&&y3==1)result|=2;
        if(z0==0&&z1==0&&z2==0&&z3==0)result|=4;
        if(z0==1&&z1==1&&z2==1&&z3==1)result|=8;
        if(x0==0&&x1==0&&x2==0&&x3==0)result|=16;
        if(x0==1&&x1==1&&x2==1&&x3==1)result|=32;
        return result;
    }

    private static boolean inside(float value){return value>=0&&value<=1;}
}

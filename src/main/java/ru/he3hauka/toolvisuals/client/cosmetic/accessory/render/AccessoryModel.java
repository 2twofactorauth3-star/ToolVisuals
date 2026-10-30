package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

public final class AccessoryModel {
    public final AccessoryMesh full,base,detail;
    public final float pivotX,pivotY,pivotZ;
    public AccessoryModel(AccessoryMesh mesh,int split,double x,double y,double z){
        full=mesh;base=mesh.slice(0,split);detail=mesh.slice(split,mesh.size-split);pivotX=(float)x;pivotY=(float)y;pivotZ=(float)z;
    }
}

package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.model.*;
import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;

public final class AccessoryModels {
    private static final AccessoryModel[][] BACK=new AccessoryModel[AccessoryLook.Back.values().length][2],HEAD=new AccessoryModel[AccessoryLook.Head.values().length][2];
    private AccessoryModels(){}
    public static synchronized AccessoryModel wing(AccessoryLook.Back type,boolean far){int i=type.ordinal(),lod=far?1:0;if(BACK[i][lod]==null)BACK[i][lod]=WingModels.build(type,far);return BACK[i][lod];}
    public static synchronized AccessoryModel headpiece(AccessoryLook.Head type,boolean far){int i=type.ordinal(),lod=far?1:0;if(HEAD[i][lod]==null)HEAD[i][lod]=HeadModels.build(type,far);return HEAD[i][lod];}
    public static AccessoryMesh back(AccessoryLook.Back type,boolean far){return wing(type,far).full;}
    public static AccessoryMesh head(AccessoryLook.Head type,boolean far){return headpiece(type,far).full;}
}

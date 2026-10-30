package ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation;

import net.minecraft.world.entity.*;

public final class CosmeticMovement {
    private CosmeticMovement(){}
    public static MotionInput read(Entity entity){
        double dx=entity.getX()-entity.xOld,dy=entity.getY()-entity.yOld,dz=entity.getZ()-entity.zOld;
        boolean teleport=dx*dx+dy*dy+dz*dz>16;
        if(entity instanceof LivingEntity actor)return new MotionInput(Math.hypot(dx,dz),dy,angle(actor.yBodyRot-actor.yBodyRotO)/35,angle(actor.yHeadRot-actor.yHeadRotO)/40,actor.onGround(),actor.isSprinting(),actor.isFallFlying(),actor.isSwimming()||actor.isInWater(),actor.isCrouching(),teleport);
        return new MotionInput(0,0,0,0,true,false,false,false,false,false);
    }
    private static double angle(double degrees){double value=degrees%360;return value>=180?value-360:value< -180?value+360:value;}
}

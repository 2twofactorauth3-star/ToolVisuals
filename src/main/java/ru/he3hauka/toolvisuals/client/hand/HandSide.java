package ru.he3hauka.toolvisuals.client.hand;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public enum HandSide {
    RIGHT(HumanoidArm.RIGHT),LEFT(HumanoidArm.LEFT);
    public final HumanoidArm arm;
    HandSide(HumanoidArm arm){this.arm=arm;}
    public HandSide opposite(){return this==RIGHT?LEFT:RIGHT;}
    public static HandSide of(HumanoidArm arm){return arm==HumanoidArm.LEFT?LEFT:RIGHT;}
    public static HandSide of(InteractionHand hand,HumanoidArm mainArm){return of(hand==InteractionHand.MAIN_HAND?mainArm:mainArm.getOpposite());}
}

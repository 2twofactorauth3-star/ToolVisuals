package ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation;

public final class MotionInput {
    public final double speed,vertical,turn,headTurn;
    public final boolean grounded,sprinting,gliding,swimming,crouching,teleport;
    public MotionInput(double speed,double vertical,double turn,double headTurn,boolean grounded,boolean sprinting,boolean gliding,boolean swimming,boolean crouching,boolean teleport){
        this.speed=bounded(speed,0,1.5);this.vertical=bounded(vertical,-1.5,1.5);this.turn=bounded(turn,-1,1);this.headTurn=bounded(headTurn,-1,1);
        this.grounded=grounded;this.sprinting=sprinting;this.gliding=gliding;this.swimming=swimming;this.crouching=crouching;this.teleport=teleport;
    }
    public static double bounded(double value,double min,double max){return Double.isFinite(value)?Math.clamp(value,min,max):0;}
}

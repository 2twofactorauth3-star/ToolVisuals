package ru.he3hauka.toolvisuals.client.animation.movement;

import ru.he3hauka.toolvisuals.client.animation.pose.PlayerPose;

import static ru.he3hauka.toolvisuals.client.animation.pose.PlayerPose.limb;

public final class MovementPoses {
    public enum Activity { NONE,EAT,DRINK,MAP,BLOCK,CLIMB,CRAWL,ROW,RIDE,INSPECT }
    public record Input(Activity activity,boolean left,double phase,float pitch,float rowingRight,float rowingLeft){
        public Input{java.util.Objects.requireNonNull(activity);if(!Double.isFinite(phase)||!Float.isFinite(pitch)||!Float.isFinite(rowingRight)||!Float.isFinite(rowingLeft))throw new IllegalArgumentException("Invalid movement pose input");}
    }
    private MovementPoses(){}
    public static PlayerPose sample(Input input){
        double p=input.phase,pitch=Math.toRadians(Math.clamp(input.pitch,-80,80)),wave=Math.sin(p);
        PlayerPose.Limb right=null,left=null,rightLeg=null,leftLeg=null;
        switch(input.activity){
            case NONE -> {return PlayerPose.EMPTY;}
            case EAT,DRINK -> {
                var arm=limb((input.activity==Activity.EAT?-1.45:-1.85)+pitch*.55+(input.activity==Activity.EAT?wave*.07:0),input.left?.4:-.4,input.left?.15:-.15);
                if(input.left)left=arm;else right=arm;
            }
            case MAP -> {right=limb(-1.1+pitch*.5,-.16,.2);left=limb(-1.1+pitch*.5,.16,-.2);}
            case BLOCK -> {var arm=limb(-1.3+pitch*.65,input.left?.6:-.6,input.left?-.12:.12);if(input.left)left=arm;else right=arm;}
            case CLIMB -> {right=limb(-2.25+wave*.5,-.1,.16);left=limb(-2.25-wave*.5,.1,-.16);rightLeg=limb(-.2-wave*.5,0,.08);leftLeg=limb(-.2+wave*.5,0,-.08);}
            case CRAWL -> {right=limb(-2.15+wave*.65,-.12,.1);left=limb(-2.15-wave*.65,.12,-.1);rightLeg=limb(wave*.4,0,.05);leftLeg=limb(-wave*.4,0,-.05);}
            case ROW -> {right=limb(-1.05+Math.cos(input.rowingRight)*.5,-.25+Math.sin(input.rowingRight)*.3,.1);left=limb(-1.05+Math.cos(input.rowingLeft)*.5,.25-Math.sin(input.rowingLeft)*.3,-.1);}
            case RIDE -> {right=limb(-.85+wave*.025,-.12,.1);left=limb(-.85+wave*.025,.12,-.1);}
            case INSPECT -> {var arm=limb(-.9+pitch*.4,input.left?.25:-.25,input.left?-.28:.28);if(input.left)left=arm;else right=arm;}
        }
        return new PlayerPose(null,null,right,left,rightLeg,leftLeg);
    }
}

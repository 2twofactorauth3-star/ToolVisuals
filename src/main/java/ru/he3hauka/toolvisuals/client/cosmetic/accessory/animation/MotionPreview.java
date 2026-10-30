package ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation;

public final class MotionPreview {
    public enum Action { STILL,WALK,RUN,JUMP,FALL,GLIDE,SWIM,CROUCH }
    private MotionPreview(){}
    public static MotionInput input(Action action,long tick,double turn){
        double jump=(Math.floorMod(tick,30))/30d;boolean grounded=action!=Action.FALL&&action!=Action.GLIDE&&action!=Action.SWIM&&(action!=Action.JUMP||jump>.84);
        double speed=switch(action){case WALK->.17;case RUN->.28;case JUMP->.18;case GLIDE->.70;case SWIM->.12;case CROUCH->.06;default->0;};
        double vertical=switch(action){case JUMP->grounded?0:.42-jump*.95;case FALL->-.65;case GLIDE->-.025;default->0;};
        return new MotionInput(speed,vertical,turn/35,turn/40,grounded,action==Action.RUN,action==Action.GLIDE,action==Action.SWIM,action==Action.CROUCH,false);
    }
    public static float jumpHeight(Action action,double ticks){if(action!=Action.JUMP)return 0;double phase=(ticks%30)/30;return phase>.84?0:(float)Math.sin(phase/.84*Math.PI)*.24f;}
}

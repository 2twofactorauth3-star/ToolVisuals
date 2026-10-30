package ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation;

public final class MotionSample {
    static final int MOVE=0,RUN=1,RISE=2,FALL=3,GLIDE=4,SWIM=5,CROUCH=6,TURN=7,HEAD_TURN=8,LANDING=9,WEIGHT=10,HEAD_WEIGHT=11,CHANNELS=12;
    private static final double[] ZERO=new double[CHANNELS];
    public static final MotionSample IDLE=new MotionSample(false,0,0,ZERO,ZERO,0);
    public final boolean tracked;
    public final double cycles,stride,move,run,rise,fall,glide,swim,crouch,turn,headTurn,landing,weight,headWeight;
    MotionSample(boolean tracked,double cycles,double stride,double[] previous,double[] current,double t){
        this.tracked=tracked;this.cycles=cycles;this.stride=stride;
        move=value(previous,current,MOVE,t);run=value(previous,current,RUN,t);rise=value(previous,current,RISE,t);fall=value(previous,current,FALL,t);glide=value(previous,current,GLIDE,t);swim=value(previous,current,SWIM,t);crouch=value(previous,current,CROUCH,t);turn=value(previous,current,TURN,t);headTurn=value(previous,current,HEAD_TURN,t);landing=value(previous,current,LANDING,t);weight=value(previous,current,WEIGHT,t);headWeight=value(previous,current,HEAD_WEIGHT,t);
    }
    private static double value(double[] a,double[] b,int channel,double t){return a[channel]+(b[channel]-a[channel])*t;}
}

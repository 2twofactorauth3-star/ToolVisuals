package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation.*;
import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;

public final class AccessoryFrame {
    public final AccessoryLook look;
    public final float phase,headRotation,headDetailRotation,backDetailRotation,wingHinge,wingFlex,wingRoll;
    public final float wingPitch,wingBank,wingBankRoll,wingLift,headPitch,headYaw,headRoll,headLift;
    public final boolean far,backVisible,hideCape;
    private final int[] backColors,headColors;
    public AccessoryFrame(AccessoryLook look,double seconds,double seed,boolean far,boolean backVisible,boolean hideCape){
        this(look,seconds,seed,far,backVisible,hideCape,MotionSample.IDLE);
    }
    public AccessoryFrame(AccessoryLook look,double seconds,double seed,boolean far,boolean backVisible,boolean hideCape,MotionSample motion){
        this.look=look;this.far=far;this.backVisible=backVisible;this.hideCape=hideCape&&backVisible&&look.back()!=AccessoryLook.Back.NONE;
        double cycles=motion.tracked?motion.cycles:seconds*look.speed()*CosmeticMotion.frequency(look)+seed;
        phase=look.animated()?(float)((cycles%1)*Math.TAU):0;
        headRotation=look.animated()?(float)(((seconds*look.speed()*.08+seed)%1)*Math.TAU):0;
        headDetailRotation=look.animated()?(float)(((seconds*look.speed()*-.14+seed)%1)*Math.TAU):0;
        backDetailRotation=look.animated()?(float)(((seconds*look.speed()*.055+seed)%1)*Math.TAU):0;
        double weight=look.animated()?motion.weight:0;
        double move=motion.move*weight,run=motion.run*weight,rise=motion.rise*weight,fall=motion.fall*weight,glide=motion.glide*weight,swim=motion.swim*weight,crouch=motion.crouch*weight,turn=motion.turn*weight,landing=motion.landing*weight;
        var profile=BackMotion.of(look.back());
        float amplitude=(float)profile.amplitude;
        amplitude*=(float)((1+.55*move+.35*run+.35*rise+.70*fall)*(1-.53*glide)*(1-.45*swim)*(1-.30*crouch));
        float rest=(float)profile.rest;
        move*=profile.flexibility;run*=profile.flexibility;rise*=profile.flexibility;fall*=profile.flexibility;
        glide*=profile.flexibility;swim*=profile.flexibility;crouch*=profile.flexibility;turn*=profile.flexibility;landing*=profile.flexibility;
        double stride=motion.stride*Math.TAU,bob=Math.sin(stride*2);
        wingHinge=rest+(look.animated()?(float)Math.sin(phase)*amplitude:0)+(float)(-.06*move-.22*run-.38*crouch-.48*swim+(.04-rest)*glide+.04*fall+.02*rise+.10*landing);
        wingFlex=(look.animated()?(float)Math.sin(phase-.65)*amplitude*.85f:0)+(float)(-.10*run-.16*swim-.12*crouch+.10*rise-.08*fall-.08*landing);
        wingRoll=(look.animated()?(float)Math.sin(phase+.35)*amplitude*.12f:0)+(float)(-.08*rise-.10*fall+.07*crouch+.04*swim+Math.sin(stride)*(.018*move+.015*run)*(1-.6*glide));
        wingPitch=(float)(-.04*move-.11*run+.10*rise-.06*fall+.11*swim+.07*landing);wingBank=(float)(turn*.08);wingBankRoll=(float)(-turn*.06);
        wingLift=(float)(bob*.007*move-landing*.01);
        boolean floating=look.head()==AccessoryLook.Head.HALO||look.head()==AccessoryLook.Head.ASTROLABE;
        double head=weight*motion.headWeight*(look.head()==AccessoryLook.Head.HEADPHONES?.25:look.head()==AccessoryLook.Head.CROWN?.45:floating?1.15:.75);
        headPitch=(float)Math.clamp(head*(-.03*motion.rise+.025*motion.fall+.045*motion.landing+bob*.010*motion.move+.04*motion.swim),-.10,.10);
        headYaw=(float)Math.clamp(head*(-.04*motion.headTurn-.025*motion.turn),-.09,.09);
        headRoll=(float)Math.clamp(head*(-.035*motion.turn-.045*motion.headTurn+Math.sin(stride)*.010*motion.move),-.09,.09);
        headLift=floating?(float)(head*(.008*bob*motion.move+.014*motion.landing+.020*motion.glide)):0;
        backColors=AccessoryShading.paints(look.backColors(),seconds);headColors=AccessoryShading.paints(look.headColors(),seconds);
    }
    int[] paints(boolean head){return head?headColors:backColors;}
    public int color(boolean head,int tone,float shade){
        return paints(head)[tone*32+Math.clamp(Math.round(shade*31),0,31)];
    }
}

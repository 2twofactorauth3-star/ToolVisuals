package ru.he3hauka.toolvisuals.client.world.ambience;

import net.minecraft.world.phys.Vec3;
import ru.he3hauka.toolvisuals.client.effects.EffectTimeline;

public final class SeasonParticles {
    public record Snapshot(EffectTimeline.Sprite[] sprites,int color) {}
    private static final Snapshot EMPTY=new Snapshot(new EffectTimeline.Sprite[0],0);
    private static final EffectTimeline TIMELINE=new EffectTimeline();
    private static Season season;
    private static Object world;
    private SeasonParticles() {}

    public static void tick(Object nextWorld,Season nextSeason,long time,boolean enabled){
        if(!enabled||world!=nextWorld||season!=nextSeason)TIMELINE.clear();
        world=enabled?nextWorld:null;season=enabled?nextSeason:null;
        TIMELINE.tick(time,0,64,0,false,false,enabled,false);
    }
    public static boolean full(){return TIMELINE.spriteCount()>=64;}
    public static void spawn(Vec3 position,long time,float variation){
        if(season==null||world==null)return;
        int kind=switch(season){case WINTER->2;case SUMMER->0;case SPRING->5;case AUTUMN->4;};
        float size=switch(season){case WINTER->.075f;case SUMMER->.025f;case SPRING->.065f;case AUTUMN->.08f;};
        Vec3 velocity=season==Season.SUMMER?new Vec3(.008,.004,.003):new Vec3(.009,-.034,.004);
        TIMELINE.sprite(new EffectTimeline.Sprite(position,velocity,time,season==Season.SUMMER?140:season==Season.AUTUMN?120:100,size*(.85f+variation*.3f),kind,variation*6.283185f,false),64);
    }
    public static Snapshot snapshot(){
        if(TIMELINE.spriteCount()==0)return EMPTY;
        int color=season==null?0:switch(season){case WINTER->0xe1efff;case SUMMER->0xe8e990;case SPRING->0xf2a7bc;case AUTUMN->0xd99537;};
        return new Snapshot(TIMELINE.snapshot().ambient(),color);
    }
}

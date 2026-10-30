package ru.he3hauka.toolvisuals.client.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.effects.render.EffectPipeline;
import ru.he3hauka.toolvisuals.client.effects.render.EffectRenderer;
import ru.he3hauka.toolvisuals.client.hud.target.VisualTarget;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.scene.NearbyEntities;
import ru.he3hauka.toolvisuals.client.world.VisualWorld;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class WorldEffects {
    private static final EffectTimeline TIMELINE=new EffectTimeline();
    private static final Map<Integer,Integer> HURT=new HashMap<>();
    private static final Set<Integer> SEEN=new HashSet<>();
    private static ClientLevel level;
    private static Vec3 previous;
    private static boolean grounded;
    private static int ticks;
    private WorldEffects() {}
    public static void initialize(){EffectPipeline.initialize();ClientTickEvents.END_CLIENT_TICK.register(WorldEffects::tick);}
    public static void extract(LevelRenderState state){
        var client=Minecraft.getInstance();
        if(client.player==null||client.level==null||client.level!=level)EffectRenderer.clear();
        else EffectRenderer.extract(state,TIMELINE);
    }
    private static void tick(Minecraft client){
        long started=Diagnostics.start(Diagnostics.Section.EFFECT_TICK);
        try{update(client);}finally{Diagnostics.end(Diagnostics.Section.EFFECT_TICK,started);}
    }
    private static void update(Minecraft client){
        if(client.level!=level||client.player==null){
            level=client.level;previous=null;grounded=false;ticks=0;HURT.clear();SEEN.clear();TIMELINE.clear();EffectRenderer.clear();
        }
        VisualTarget.tick(client);VisualWorld.tick(client);
        if(client.player==null||level==null){ru.he3hauka.toolvisuals.client.hud.ExtraHud.reset();return;}
        if(client.isPaused())return;
        var config=ToolVisualsClient.CONFIG;
        var player=client.player;
        Vec3 pos=player.position();
        long now=level.getGameTime();
        int circleLimit=EffectBudget.limit(OPT_MAX_CIRCLES,8),spriteLimit=EffectBudget.limit(OPT_MAX_PARTICLES,1024);
        TIMELINE.tick(now,circleLimit,spriteLimit,(int)config.value(TRAIL_DURATION),config.active(JUMP_CIRCLES),config.active(TRAILS),config.active(WORLD_PARTICLES),config.active(HIT_PARTICLES));
        if(config.active(WORLD_PARTICLES)&&++ticks%2==0){
            var random=level.getRandom();
            float radius=config.number(AMBIENT_RADIUS);
            for(int i=0;i<config.value(AMBIENT_DENSITY);i++){
                double x=pos.x+(random.nextDouble()*2-1)*radius,z=pos.z+(random.nextDouble()*2-1)*radius,y=pos.y+random.nextDouble()*6;
                if((x-pos.x)*(x-pos.x)+(z-pos.z)*(z-pos.z)<9||!level.getBlockState(BlockPos.containing(x,y,z)).isAir())continue;
                TIMELINE.sprite(new EffectTimeline.Sprite(new Vec3(x,y,z),new Vec3((random.nextDouble()-.5)*.012,-.014,(random.nextDouble()-.5)*.012),now,
                        60+random.nextInt(40),.12f+random.nextFloat()*.06f,(int)config.value(AMBIENT_KIND),random.nextFloat()*6.28f,false),spriteLimit);
            }
        }
        if(config.active(JUMP_CIRCLES)&&previous!=null&&grounded&&!player.onGround()&&player.getDeltaMovement().y>.08&&!player.isInWater()&&!player.getAbilities().flying){
            TIMELINE.jump(previous.add(0,.035,0),now,(int)config.value(CIRCLE_DURATION),config.number(CIRCLE_RADIUS),circleLimit);
            ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(JUMP_CIRCLES,"jump_effect_requested","");
        }
        if(config.active(TRAILS)&&previous!=null)TIMELINE.trail(previous,pos,now,(int)config.value(TRAIL_DENSITY));
        if(config.active(HIT_PARTICLES)||config.active(HIT_BUBBLES)){
            SEEN.clear();int bursts=0;
            for(var entity:NearbyEntities.get(client)){
                if(!(entity instanceof LivingEntity living)||entity.isRemoved()||entity.level()!=level||entity.distanceToSqr(player)>32*32)continue;
                SEEN.add(entity.getId());Integer old=HURT.put(entity.getId(),living.hurtTime);
                if(old==null||living.hurtTime<=old||living.hurtTime<5||bursts++>=8)continue;
                if(entity!=player||config.flag(BUBBLE_SELF))VisualWorld.hit(living);
                if(config.active(HIT_PARTICLES)&&(entity!=player||config.flag(HIT_SELF))){
                    if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(HIT_PARTICLES,"burst_requested","count="+(int)config.value(HIT_PARTICLE_COUNT));
                    var random=level.getRandom();
                    for(int i=0;i<config.value(HIT_PARTICLE_COUNT);i++)
                        TIMELINE.sprite(new EffectTimeline.Sprite(entity.position().add(0,entity.getBbHeight()*.6,0),
                                new Vec3((random.nextDouble()-.5)*.25,random.nextDouble()*.15,(random.nextDouble()-.5)*.25),now,12+random.nextInt(10),
                                .12f+random.nextFloat()*.06f,(int)config.value(HIT_PARTICLE_KIND),random.nextFloat()*6.28f,true),spriteLimit);
                }
            }
            HURT.keySet().retainAll(SEEN);
        }else HURT.clear();
        grounded=player.onGround();previous=pos;
    }
}

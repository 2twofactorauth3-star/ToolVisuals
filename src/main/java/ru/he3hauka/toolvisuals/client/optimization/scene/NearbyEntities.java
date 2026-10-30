package ru.he3hauka.toolvisuals.client.optimization.scene;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

public final class NearbyEntities {
    private static final TickSnapshot<Entity> CACHE=new TickSnapshot<>();
    private static long tick;
    private static Object world;
    private NearbyEntities(){}
    public static void beginTick(Minecraft client){tick++;if(world!=client.level||client.player==null){world=client.level;CACHE.clear();}}
    public static List<Entity> get(Minecraft client){
        if(client.player==null||client.level==null)return List.of();
        return CACHE.get(client.level,tick,()->{
            long started=Diagnostics.start(Diagnostics.Section.ENTITY_SCAN);var entities=new ArrayList<Entity>();
            double radius=80;
            for(var entity:client.level.entitiesForRendering())if(!entity.isRemoved()&&entity.distanceToSqr(client.player)<=radius*radius)entities.add(entity);
            Diagnostics.end(Diagnostics.Section.ENTITY_SCAN,started);return entities;
        });
    }
}

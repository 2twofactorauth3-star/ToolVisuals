package ru.he3hauka.toolvisuals.client.optimization.crystal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.mixin.entity.EntityRemovalInvoker;

public final class CrystalOptimizer {
    private static final CrystalPredictions<EndCrystal> PENDING=new CrystalPredictions<>();
    private static ClientLevel world;
    private static Object connection;
    private CrystalOptimizer(){}
    public static void clear(){PENDING.clear();world=null;connection=null;}
    private static void scope(Minecraft client){if(world!=client.level||connection!=client.getConnection()){PENDING.clear();world=client.level;connection=client.getConnection();}}
    public static void attacked(Player player,Entity target){
        var client=Minecraft.getInstance();scope(client);
        if(!(target instanceof EndCrystal crystal)||!ToolVisualsClient.CONFIG.active(VisualModule.CRYSTAL_OPTIMIZER)||player!=client.player||world==null||connection==null||target.level()!=world||target.isRemoved()||target.isInvulnerable()||player.isSpectator()||!player.isAlive())return;
        var info=client.getConnection().getPlayerInfo(player.getUUID());int ping=info==null?0:info.getLatency();
        long timeout=CrystalPredictions.timeout(ToolVisualsClient.CONFIG.value(VisualSetting.CRYSTAL_RECOVERY),ping);
        if(PENDING.begin(crystal.getId(),crystal,System.nanoTime(),timeout))world.removeEntity(crystal.getId(),Entity.RemovalReason.DISCARDED);
    }
    public static void confirmed(int id){if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;scope(Minecraft.getInstance());PENDING.confirmed(id);}
    public static void tick(Minecraft client){
        scope(client);if(world==null||client.player==null){PENDING.clear();return;}
        var ready=ToolVisualsClient.CONFIG.active(VisualModule.CRYSTAL_OPTIMIZER)&&client.player.isAlive()?PENDING.expired(System.nanoTime()):PENDING.drain();
        for(var crystal:ready)restore(client,crystal);
    }
    private static void restore(Minecraft client,EndCrystal crystal){
        if(crystal.level()!=world||crystal.getRemovalReason()!=Entity.RemovalReason.DISCARDED||world.getEntity(crystal.getId())!=null||world.getEntity(crystal.getUUID())!=null||!world.hasChunkAt(crystal.blockPosition())||client.player.distanceToSqr(crystal)>128*128)return;
        ((EntityRemovalInvoker)crystal).toolvisuals$restoreRemoval();world.addEntity(crystal);
    }
}

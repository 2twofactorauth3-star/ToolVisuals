package ru.he3hauka.toolvisuals.client.optimization.occlusion;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;

import ru.he3hauka.toolvisuals.client.hud.target.VisualTarget;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class OcclusionService {
    private record Request(Object key,AABB bounds,long revision){}
    private record Decision(AABB solid,long revision,long expires){}
    private static final int LIMIT=512;
    private static final ConcurrentHashMap<Object,Request> PENDING=new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<Object> QUEUE=new ConcurrentLinkedQueue<>();
    private static final LinkedHashMap<Object,Decision> CACHE=new LinkedHashMap<>(LIMIT,.75f,true);
    private static final Long2ByteOpenHashMap CELLS=new Long2ByteOpenHashMap();
    private static final AtomicLong REVISION=new AtomicLong();
    private static volatile Map<Object,Decision> view=Map.of();
    private static volatile long tick;
    private static Object world;
    private static boolean external;
    private static final BlockPos.MutableBlockPos POS=new BlockPos.MutableBlockPos();
    private static int cellReads;
    private static long deadline;
    private static final class Unavailable extends RuntimeException {
        private static final Unavailable INSTANCE=new Unavailable();
        private Unavailable(){super(null,null,false,false);}
    }
    private OcclusionService(){}
    public static void initialize(){
        external=FabricLoader.getInstance().isModLoaded("entityculling");
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.CHUNK_LOAD.register((level,chunk)->invalidate());
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents.CHUNK_UNLOAD.register((level,chunk)->invalidate());
    }
    public static long revision(){return REVISION.get();}
    public static String backend(){return external?"entityculling":"builtin";}
    public static void invalidate(){REVISION.incrementAndGet();view=Map.of();}
    private static boolean enabled(boolean block){return !external&&!ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active()&&Optimizations.enabled(block?OPT_BLOCK_ENTITY_CULLING:OPT_ENTITY_CULLING);}
    public static boolean hidden(Entity entity){
        if(!enabled(false))return false;var client=Minecraft.getInstance();
        if(client.player==null||entity==client.player||entity==VisualTarget.current()||entity.isRemoved()||entity.isPassenger()||entity.isVehicle()||entity.isCurrentlyGlowing()||client.shouldEntityAppearGlowing(entity)||!BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace().equals("minecraft"))return false;
        if(entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon||entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss||entity instanceof net.minecraft.world.entity.monster.Ghast||entity instanceof Display)return false;
        if(entity instanceof net.minecraft.world.entity.player.Player&&ru.he3hauka.toolvisuals.client.animation.Emotes.pose(entity.getUUID())!=null)return false;
        return hidden(entity,entity.getBoundingBox().inflate(entity instanceof net.minecraft.world.entity.player.Player?1:.5));
    }
    public static boolean hidden(BlockEntity entity,boolean global){
        if(!enabled(true)||global)return false;
        if(!(entity instanceof ChestBlockEntity||entity instanceof EnderChestBlockEntity||entity instanceof ShulkerBoxBlockEntity||entity instanceof SignBlockEntity||entity instanceof SkullBlockEntity))return false;
        return hidden(entity,new AABB(entity.getBlockPos()).inflate(.65));
    }
    private static boolean hidden(Object key,AABB bounds){
        var client=Minecraft.getInstance();if(client.level==null||client.player==null)return false;var eye=client.gameRenderer.mainCamera().position();
        double distance=bounds.getCenter().distanceToSqr(eye);if(distance<8*8||distance>96*96||bounds.getSize()>8)return false;
        long revision=REVISION.get();var decision=view.get(key);
        if(decision==null||decision.revision!=revision||tick>=decision.expires-1)
            if(PENDING.size()<LIMIT){var request=new Request(key,bounds.inflate(.15),revision);if(PENDING.putIfAbsent(key,request)==null)QUEUE.add(key);}
        if(decision==null||decision.revision!=revision||tick>=decision.expires)return false;
        boolean hidden=SolidOccluder.covers(eye,bounds,decision.solid);if(hidden)Diagnostics.count(Diagnostics.Counter.OCCLUDED,1);return hidden;
    }
    public static void tick(Minecraft client){
        if(world!=client.level){world=client.level;clear();}
        tick++;
        if(client.level==null||client.player==null||!enabled(false)&&!enabled(true)){if(!CACHE.isEmpty()||!PENDING.isEmpty())clear();return;}
        long started=Diagnostics.start(Diagnostics.Section.OCCLUSION),revision=REVISION.get();deadline=System.nanoTime()+500_000L;var eye=client.gameRenderer.mainCamera().position();
        CELLS.clear();int processed=0;Object key;
        while(processed<16&&System.nanoTime()<deadline&&(key=QUEUE.poll())!=null){
            var request=PENDING.remove(key);if(request==null||request.revision!=revision)continue;
            AABB solid=null;cellReads=0;
            try{solid=SolidOccluder.find(eye,request.bounds,(x,y,z)->solid(client,x,y,z),160);}catch(Unavailable ignored){}
            Diagnostics.count(Diagnostics.Counter.OCCLUSION_TESTS,1);
            CACHE.put(key,new Decision(solid,revision,tick+(solid==null?4:3)));processed++;
        }
        CACHE.values().removeIf(decision->tick-decision.expires>80);while(CACHE.size()>LIMIT)CACHE.remove(CACHE.keySet().iterator().next());
        if(processed>0||view.size()!=CACHE.size())view=Map.copyOf(CACHE);
        Diagnostics.end(Diagnostics.Section.OCCLUSION,started);
    }
    private static boolean solid(Minecraft client,int x,int y,int z){
        if(++cellReads>512)throw Unavailable.INSTANCE;
        if((cellReads&31)==0&&System.nanoTime()>=deadline)throw Unavailable.INSTANCE;
        long key=BlockPos.asLong(x,y,z);byte cached=CELLS.get(key);if(cached!=0)return cached==2;
        POS.set(x,y,z);if(!client.level.hasChunkAt(POS))throw Unavailable.INSTANCE;
        Diagnostics.count(Diagnostics.Counter.OCCLUSION_CELLS,1);
        boolean solid=client.level.getBlockState(POS).isSolidRender();
        if(CELLS.size()<8192)CELLS.put(key,(byte)(solid?2:1));return solid;
    }
    private static void clear(){invalidate();CACHE.clear();PENDING.clear();QUEUE.clear();CELLS.clear();}
}

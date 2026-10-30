package ru.he3hauka.toolvisuals.client.optimization.frame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.scene.NearbyEntities;
import ru.he3hauka.toolvisuals.client.optimization.texture.TextureUploads;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.OPTIMIZATION;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class FrameMonitor {
    private static final AdaptiveQuality QUALITY=new AdaptiveQuality();
    private static long previous,renderStarted,tickStarted,nextTargetRefresh;
    private static int target;
    private static volatile int detail;
    private static Object world;

    private FrameMonitor(){}

    public static int detail(){return detail;}

    public static void begin(){
        long now=System.nanoTime();
        long interval=previous==0?0:now-previous;
        previous=now;
        renderStarted=Diagnostics.start(Diagnostics.Section.RENDER);
        updateQuality(Minecraft.getInstance(),now,interval);
        TextureUploads.drain();
    }

    private static void updateQuality(Minecraft client,long now,long interval){
        var config=ToolVisualsClient.CONFIG;
        if(world!=client.level){
            world=client.level;
            ru.he3hauka.toolvisuals.client.optimization.equipment.PreparedEquipment.clear();
            QUALITY.reset();
            nextTargetRefresh=0;
        }
        boolean playing=client.player!=null&&client.level!=null&&client.isWindowActive()
                &&!client.isPaused()&&client.gui.screen()==null&&client.gui.overlay()==null;
        if(!playing||!config.active(OPTIMIZATION)||!config.flag(OPT_ADAPTIVE_QUALITY)){
            QUALITY.reset();
            detail=0;
            return;
        }
        if(now>=nextTargetRefresh){
            target=(int)config.value(OPT_ADAPTIVE_TARGET);
            int limit=client.options.framerateLimit().get();
            if(limit>0&&limit<Options.UNLIMITED_FRAMERATE_CUTOFF)target=Math.min(target,limit);
            if(client.options.enableVsync().get()){
                var mode=client.getWindow().getActiveVideoMode();
                if(mode!=null&&mode.getRefreshRate()>0)target=Math.min(target,Math.round(mode.getRefreshRate()));
            }
            nextTargetRefresh=now+1_000_000_000L;
        }
        QUALITY.sample(now,interval,target);
        detail=QUALITY.level();
    }

    public static void end(){Diagnostics.end(Diagnostics.Section.RENDER,renderStarted);}

    public static void beginTick(Minecraft client){
        tickStarted=Diagnostics.start(Diagnostics.Section.CLIENT_TICK);
        NearbyEntities.beginTick(client);
    }

    public static void endTick(Minecraft client){Diagnostics.end(Diagnostics.Section.CLIENT_TICK,tickStarted);}
}

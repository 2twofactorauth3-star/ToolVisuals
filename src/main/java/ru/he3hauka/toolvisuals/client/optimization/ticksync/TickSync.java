package ru.he3hauka.toolvisuals.client.optimization.ticksync;

import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.TICK_SYNC;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class TickSync {
    private static final boolean EXTERNAL=FabricLoader.getInstance().isModLoaded("ticksync")||FabricLoader.getInstance().isModLoaded("mcrider-sync");
    private static final TickPhase PHASE=new TickPhase();
    private static final AtomicLong ARRIVAL=new AtomicLong();
    private static volatile Object listener;
    private static Object world;
    private static volatile float shift;
    private TickSync() {}

    public static void packet(Object source){
        if(source!=listener||Minecraft.getInstance().isSameThread())return;
        long now=System.nanoTime(),previous=ARRIVAL.get();
        if(previous==0||now-previous>=2_000_000L)ARRIVAL.compareAndSet(previous,now);
    }
    public static void tick(Minecraft client){
        shift=0;
        boolean available=!EXTERNAL&&ToolVisualsClient.CONFIG.active(TICK_SYNC)&&client.level!=null&&client.player!=null
                &&client.isWindowActive()&&!client.isPaused()&&!ReplayScope.active()&&client.getSingleplayerServer()==null
                &&client.level.tickRateManager().runsNormally()&&Math.abs(client.level.tickRateManager().tickrate()-20)<.5;
        Object next=available?client.getConnection():null;
        if(next!=listener||world!=client.level||!available){listener=next;world=client.level;ARRIVAL.set(0);PHASE.reset();return;}
        shift=(float)PHASE.sample(System.nanoTime(),ARRIVAL.get(),client.getFps(),ToolVisualsClient.CONFIG.value(TICK_SYNC_MARGIN),ToolVisualsClient.CONFIG.value(TICK_SYNC_MAX_SHIFT));
    }
    public static float duration(float original){return original>=49&&original<=51?original+shift:original;}
}

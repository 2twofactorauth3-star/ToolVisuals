package ru.he3hauka.toolvisuals.client.optimization.diagnostics;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture.DiagnosticContext;

import ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService;

public final class MinecraftDiagnostics {
    private static long next,revision,configRevision=-1;
    private static java.lang.ref.WeakReference<Object> world=new java.lang.ref.WeakReference<>(null);
    private static String details="",modules="";
    private MinecraftDiagnostics() {}

    public static DiagnosticContext context(Minecraft client,boolean render) {
        if(!Diagnostics.enabled())return DiagnosticContext.INACTIVE;
        long now=System.nanoTime();
        if(world.get()!=client.level){world=new java.lang.ref.WeakReference<>(client.level);revision++;next=0;}
        var limiter=client.getFramerateLimitTracker();
        var screen=client.gui.screen();
        boolean vsync=client.options.enableVsync().get();int limit=limiter.getFramerateLimit();
        boolean loading=!render||client.gui.overlay()!=null||screen instanceof PauseScreen||screen instanceof LevelLoadingScreen;
        if(now>=next){
            next=now+1_000_000_000L;
            String particles=client.particleEngine==null?"0":client.particleEngine.countParticles();
            details=String.format(Locale.ROOT,"fps_limit=%s vsync=%s render_distance=%d simulation_distance=%d particles=%s culling=%s",
                    limit>=net.minecraft.client.Options.UNLIMITED_FRAMERATE_CUTOFF?"unlimited":String.valueOf(limit),vsync,client.options.renderDistance().get(),client.options.simulationDistance().get(),particles,OcclusionService.backend());
            var config=ToolVisualsClient.CONFIG;
            if(configRevision!=config.revision()){configRevision=config.revision();modules=java.util.Arrays.stream(VisualModule.values()).filter(config::active).map(module->module.title).collect(java.util.stream.Collectors.joining(","));}
        }
        if(limit>=net.minecraft.client.Options.UNLIMITED_FRAMERATE_CUTOFF)limit=0;
        if(vsync){var mode=client.getWindow().getActiveVideoMode();if(mode!=null&&mode.getRefreshRate()>0)limit=limit>0?Math.min(limit,Math.round(mode.getRefreshRate())):Math.round(mode.getRefreshRate());}
        return new DiagnosticContext(client.level!=null&&client.player!=null,client.isWindowActive()&&client.getWindow().isFocused(),client.getWindow().isIconified(),client.isPaused(),loading,
                screen==null?"none":screen.getClass().getSimpleName(),limiter.getThrottleReason().name(),revision,limit>0?1_000_000_000L/limit:0,details,modules);
    }
}

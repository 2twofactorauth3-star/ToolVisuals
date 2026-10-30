package ru.he3hauka.toolvisuals.client.effects.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.animation.EffectTime;
import ru.he3hauka.toolvisuals.client.effects.*;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class EffectRenderer {
    private static volatile EffectGeometry.Frame frame;
    private EffectRenderer() {}
    public static void clear(){frame=null;}
    public static void extract(LevelRenderState state,EffectTimeline timeline){
        var client=Minecraft.getInstance();
        var effects=timeline.snapshot(client.player.getPosition(state.worldPartialTicks),EffectTime.at(state.gameTime-1,state.worldPartialTicks));
        if(effects.empty()){frame=null;return;}
        var config=ToolVisualsClient.CONFIG;
        Vec3 right=new Vec3(new Vector3f(1,0,0).rotate(state.cameraRenderState.orientation));
        Vec3 up=new Vec3(new Vector3f(0,1,0).rotate(state.cameraRenderState.orientation));
        frame=new EffectGeometry.Frame(effects,EffectTime.at(state.gameTime,state.worldPartialTicks),state.cameraRenderState.pos,right,up,
                config.color(CIRCLE_COLOR),config.color(TRAIL_COLOR),config.color(AMBIENT_COLOR),config.color(HIT_PARTICLE_COLOR),
                config.number(CIRCLE_WIDTH),config.number(CIRCLE_GLOW),config.number(TRAIL_SIZE),(int)config.value(TRAIL_DURATION),Math.max(EffectBudget.quality(),FrameMonitor.detail()));
    }
    public static void submit(SubmitNodeCollector collector){
        var current=frame;if(current==null)return;
        collector.submitCustomGeometry(new PoseStack(),EffectPipeline.TYPE,(matrix,vertices)->{
            long started=Diagnostics.start(Diagnostics.Section.WORLD_SUBMIT);
            try{EffectGeometry.emit(current,(x,y,z,color)->vertices.addVertex(matrix,x,y,z).setColor(color));}
            finally{Diagnostics.end(Diagnostics.Section.WORLD_SUBMIT,started);}
        });
    }
}

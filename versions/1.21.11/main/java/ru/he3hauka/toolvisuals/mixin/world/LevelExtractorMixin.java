package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.ParticlesRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.world.VisualWorld;
import ru.he3hauka.toolvisuals.client.animation.death.DeathAscension;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.WORLD_CUSTOMIZER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

@Mixin(LevelRenderer.class)
public abstract class LevelExtractorMixin {
    @Shadow @Final private LevelRenderState levelRenderState;
    @Shadow @Final private ParticlesRenderState particlesRenderState;
    @Inject(method={"blockChanged","setBlocksDirty","setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V","setLevel","onResourceManagerReload"},at=@At("HEAD"))
    private void toolvisuals$invalidate(CallbackInfo callback){ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService.invalidate();}
    @Inject(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;)V"))
    private void toolvisuals$world(CallbackInfo callback) {
        var state=levelRenderState;var config=ToolVisualsClient.CONFIG;var sky=state.skyRenderState;
        if(config.active(WORLD_CUSTOMIZER)) {
            int time=(int)config.value(TIME);
            if(time!=0) {
                sky.sunAngle=time==1?0:time==2?85:180;sky.moonAngle=sky.sunAngle+180;sky.starAngle=sky.sunAngle;sky.starBrightness=time==3?.75f:0;
                sky.skyColor=time==3?0xff060917:time==2?0xffad5266:0xff6ba6ff;
            }
            if(config.value(SKY_TINT)>0) {
                int rgb=config.color(SKY_TINT);float strength=time==3?.15f:.75f;
                sky.skyColor=0xff000000|(int)((rgb>>16&255)*strength)<<16|(int)((rgb>>8&255)*strength)<<8|(int)((rgb&255)*strength);
            }
            if(config.flag(CLEAR_WEATHER)){state.weatherRenderState.rainColumns.clear();state.weatherRenderState.snowColumns.clear();sky.rainBrightness=1;}
        }
        if(NoRender.hides(RAIN))state.weatherRenderState.rainColumns.clear();
        if(NoRender.hides(SNOW))state.weatherRenderState.snowColumns.clear();
        if(NoRender.hides(SKY))sky.skybox=DimensionType.Skybox.NONE;
        if(NoRender.hides(STARS))sky.starBrightness=0;
        if(NoRender.hides(END_FLASH))sky.endFlashIntensity=0;
        if(NoRender.hides(WORLD_BORDER))state.worldBorderRenderState.alpha=0;
        if(NoRender.hides(BLOCK_OUTLINE)||ru.he3hauka.toolvisuals.client.render.blockoverlay.BlockOverlay.active())state.blockOutlineRenderState=null;
        if(NoRender.hides(BLOCK_BREAKING))state.blockBreakingRenderStates.clear();
        if(NoRender.hides(ALL_PARTICLES))particlesRenderState.reset();
        VisualWorld.extract(state);DeathAscension.extract(state);
    }
    @Inject(method="addCloudsPass",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$clouds(CallbackInfo callback){if(NoRender.hides(CLOUDS))callback.cancel();}
}

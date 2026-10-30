package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Inject(method="isEntityVisible",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$occludeObjects(Entity entity,Frustum frustum,double x,double y,double z,float partial,long section,CallbackInfoReturnable<Boolean> callback){
        if(callback.getReturnValueZ()&&!(entity instanceof net.minecraft.world.entity.LivingEntity)&&!entity.hasCustomName()&&ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService.hidden(entity))callback.setReturnValue(false);
    }
    @Inject(method={"blockChanged","setBlocksDirty","setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V","setLevel","onResourceManagerReload"},at=@At("HEAD"))
    private void toolvisuals$invalidateOccluders(CallbackInfo callback){ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService.invalidate();}
    @Shadow @Final private LevelRenderState levelRenderState;

    @Inject(method = "isEntityVisible", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$hideEntity(Entity entity, Frustum frustum, double x, double y, double z,
            float partialTick, long sectionPosition, CallbackInfoReturnable<Boolean> cir) {
        if (NoRender.hidesEntity(entity)) cir.setReturnValue(false);
    }

    @Inject(method = "extract", at = @At("TAIL"))
    private void toolvisuals$filterRenderState(CallbackInfo ci) {
        var state = levelRenderState;
        var config = ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG;
        if (config.active(ru.he3hauka.toolvisuals.client.module.VisualModule.WORLD_CUSTOMIZER)) {
            var sky = state.skyRenderState;
            int time = (int)config.value(ru.he3hauka.toolvisuals.client.module.VisualSetting.TIME);
            if (time != 0) {
                sky.sunAngle = time == 1 ? 0 : time == 2 ? 85 : 180;
                sky.moonAngle = sky.sunAngle + 180;
                sky.starAngle = sky.sunAngle;
                sky.starBrightness = time == 3 ? .75f : 0;
                sky.skyColor = time == 3 ? new org.joml.Vector3f(.025f, .035f, .09f)
                        : time == 2 ? new org.joml.Vector3f(.68f, .32f, .4f) : new org.joml.Vector3f(.42f, .65f, 1);
            }
            int tint = (int)config.value(ru.he3hauka.toolvisuals.client.module.VisualSetting.SKY_TINT);
            if (tint > 0) {
                int rgb = config.color(ru.he3hauka.toolvisuals.client.module.VisualSetting.SKY_TINT);
                var color = new org.joml.Vector3f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f);
                sky.skyColor = color.mul(time == 3 ? .15f : .75f);
            }
            if (config.flag(ru.he3hauka.toolvisuals.client.module.VisualSetting.CLEAR_WEATHER)) {
                state.weatherRenderState.rainColumns.clear(); state.weatherRenderState.snowColumns.clear();
                sky.rainBrightness = 1;
            }
        }
        if (NoRender.hides(RAIN)) state.weatherRenderState.rainColumns.clear();
        if (NoRender.hides(SNOW)) state.weatherRenderState.snowColumns.clear();
        if (NoRender.hides(CLOUDS)) state.cloudColor = 0;
        if (NoRender.hides(SKY)) state.skyRenderState.skybox = DimensionType.Skybox.NONE;
        if (NoRender.hides(STARS)) state.skyRenderState.starBrightness = 0;
        if (NoRender.hides(END_FLASH)) state.skyRenderState.endFlashIntensity = 0;
        if (NoRender.hides(WORLD_BORDER)) state.worldBorderRenderState.alpha = 0;
        if (NoRender.hides(BLOCK_OUTLINE)||ru.he3hauka.toolvisuals.client.render.blockoverlay.BlockOverlay.active()) state.blockOutlineRenderState = null;
        if (NoRender.hides(BLOCK_BREAKING)) state.blockBreakingRenderStates.clear();
        if (NoRender.hides(ALL_PARTICLES)) state.particlesRenderState.reset();
        if (NoRender.hides(CROSSHAIR) || NoRender.hides(ALL_HUD)) state.render3dCrosshair = false;
        var player = state.playerRenderState;
        if (NoRender.hides(PORTAL_DISTORTION)) player.portalEffectIntensity = 0;
        if (NoRender.hides(NAUSEA)) player.nauseaEffectIntensity = 0;
        if (NoRender.hides(FIRE_OVERLAY)) player.isOnFire = false;
        if (NoRender.hides(WATER_OVERLAY)) player.waterOverlay = null;
        if (ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG.flag(ru.he3hauka.toolvisuals.client.module.VisualSetting.NO_FLUID_OVERLAY)
                && ru.he3hauka.toolvisuals.client.render.filter.NoFluid.clears(net.minecraft.client.Minecraft.getInstance().gameRenderer.mainCamera().getFluidInCamera())) {
            player.waterOverlay = null; player.isOnFire = false;
        }
        if (NoRender.hides(TOTEM_ANIMATION)) player.itemActivation = null;
        ru.he3hauka.toolvisuals.client.world.VisualWorld.extract(state);
        ru.he3hauka.toolvisuals.client.animation.death.DeathAscension.extract(state);
    }
}


package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelExtractorMixin {
    @Inject(method={"blockChanged","setBlocksDirty","setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V","setLevel","onResourceManagerReload"},at=@At("HEAD"))
    private void toolvisuals$invalidateOccluders(CallbackInfo callback){ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService.invalidate();}
}

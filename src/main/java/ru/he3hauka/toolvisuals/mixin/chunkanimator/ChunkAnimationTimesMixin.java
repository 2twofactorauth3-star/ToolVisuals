package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimationTimes;
import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager", remap = false)
public abstract class ChunkAnimationTimesMixin {
    @Unique private final ChunkAnimationTimes toolvisuals$animationTimes = new ChunkAnimationTimes();

    @WrapMethod(method = "writeMeshTimes")
    private void toolvisuals$keepActiveStart(int region, int section, int time, Operation<Void> original) {
        if (ChunkAnimations.enabled()) time = toolvisuals$animationTimes.time(region, section, time,
                System.nanoTime(), ChunkAnimations.durationMillis() * 1_000_000L);
        else toolvisuals$animationTimes.clear();
        original.call(region, section, time);
    }
    @Inject(method = "clearRegionTimes", at = @At("HEAD"))
    private void toolvisuals$forgetRegion(int region, CallbackInfo ci) { toolvisuals$animationTimes.clearRegion(region); }
    @Inject(method = "delete", at = @At("HEAD"))
    private void toolvisuals$forgetWorld(CallbackInfo ci) { toolvisuals$animationTimes.clear(); }
}

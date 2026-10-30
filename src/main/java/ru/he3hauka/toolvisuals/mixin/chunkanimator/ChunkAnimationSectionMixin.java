package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSection", remap = false)
public abstract class ChunkAnimationSectionMixin {
    @Unique private long toolvisuals$lastAnimationNanos;

    @ModifyReturnValue(method = "consumeFade", at = @At("RETURN"))
    private boolean toolvisuals$animateRebuiltSection(boolean firstBuild) {
        if (!ChunkAnimations.enabled()) return firstBuild;
        long now = System.nanoTime();
         
        boolean animate = firstBuild || ChunkAnimations.animateUpdates()
                && now - toolvisuals$lastAnimationNanos >= ChunkAnimations.durationMillis() * 1_000_000L;
        if (animate) toolvisuals$lastAnimationNanos = now;
        return animate;
    }
}

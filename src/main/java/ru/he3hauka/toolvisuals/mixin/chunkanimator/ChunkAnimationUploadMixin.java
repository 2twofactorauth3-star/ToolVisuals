package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager", remap = false)
public abstract class ChunkAnimationUploadMixin {
    @ModifyConstant(method = "uploadResults(Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegion;Ljava/util/Collection;Lnet/caffeinemc/mods/sodium/client/render/chunk/UniformBufferManager;)V",
            constant = @Constant(doubleValue = 768.0))
    private double toolvisuals$animateNearby(double original) { return ChunkAnimations.enabled() ? 0 : original; }
}

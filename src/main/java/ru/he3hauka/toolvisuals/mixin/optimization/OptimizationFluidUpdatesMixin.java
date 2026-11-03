package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.particles.ParticleFluids;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_PARTICLE_FLUID;

@Mixin(ClientLevel.class)
public abstract class OptimizationFluidUpdatesMixin {
    @Inject(method = {"setBlocksDirty", "sendBlockUpdated", "onChunkLoaded", "unload"}, at = @At("TAIL"))
    private void toolvisuals$invalidateFluid(CallbackInfo callback) {
        if (Optimizations.enabled(OPT_PARTICLE_FLUID)) ParticleFluids.invalidate();
    }
}

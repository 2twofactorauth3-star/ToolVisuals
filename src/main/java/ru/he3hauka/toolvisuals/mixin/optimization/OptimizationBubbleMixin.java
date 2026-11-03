package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleColumnUpParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.particles.ParticleFluids;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_PARTICLE_FLUID;

@Mixin(BubbleColumnUpParticle.class)
public abstract class OptimizationBubbleMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/multiplayer/ClientLevel;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    private FluidState toolvisuals$reuseFluid(ClientLevel world, BlockPos position, Operation<FluidState> original) {
        if (!Optimizations.enabled(OPT_PARTICLE_FLUID)) return original.call(world, position);
        var cache = ParticleFluids.current(world, world.getGameTime());
        long key = position.asLong();
        FluidState fluid = cache.get(key);
        if (fluid != null) return fluid;
        fluid = original.call(world, position);
        cache.put(key, fluid);
        return fluid;
    }
}

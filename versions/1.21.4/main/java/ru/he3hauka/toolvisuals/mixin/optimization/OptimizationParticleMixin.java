 
 
package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.*;

import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.particles.ParticleLightCache;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_PARTICLE_LIGHT;

@Mixin(Particle.class)
public abstract class OptimizationParticleMixin {
    @Shadow @Final protected ClientLevel level;
    @Shadow protected double x,y,z;
    @Unique private static final boolean toolvisuals$externalParticleCore = FabricLoader.getInstance().isModLoaded("particle_core");
    @Unique private long toolvisuals$lightTick = Long.MIN_VALUE;
    @Unique private int toolvisuals$lightX,toolvisuals$lightY,toolvisuals$lightZ,toolvisuals$light;
    @WrapMethod(method="getLightColor")
    private int toolvisuals$reuseLight(float partial,Operation<Integer> original) {
        if (toolvisuals$externalParticleCore || !Optimizations.enabled(OPT_PARTICLE_LIGHT)) { toolvisuals$lightTick = Long.MIN_VALUE; return original.call(partial); }
        long tick = level.getGameTime(); int bx = Mth.floor(x), by = Mth.floor(y), bz = Mth.floor(z);
        if (tick == toolvisuals$lightTick && bx == toolvisuals$lightX && by == toolvisuals$lightY && bz == toolvisuals$lightZ) return toolvisuals$light;
        int light = original.call(partial);
        toolvisuals$lightX = bx; toolvisuals$lightY = by; toolvisuals$lightZ = bz;
        toolvisuals$light = light; toolvisuals$lightTick = tick; return light;
    }

    @WrapOperation(method="getLightColor", at=@At(value="INVOKE",
            target="Lnet/minecraft/client/renderer/LevelRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    private int toolvisuals$sharedLight(BlockAndTintGetter world,BlockPos position,Operation<Integer> original) {
        if (toolvisuals$externalParticleCore || !Optimizations.enabled(OPT_PARTICLE_LIGHT) || world != level)
            return original.call(world,position);
        var cache = ParticleLightCache.current(level,level.getGameTime());
        long key = position.asLong();
        int light = cache.get(key);
        if (light == ParticleLightCache.MISSING) {
            light = original.call(world,position);
            cache.put(key,light);
        }
        return light;
    }
}

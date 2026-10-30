package ru.he3hauka.toolvisuals.mixin.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$hideParticleType(ParticleOptions options, double x, double y, double z,
            double dx, double dy, double dz, CallbackInfoReturnable<Particle> cir) {
        if (NoRender.hidesParticle(BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType()).getPath())) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$hideParticle(Particle particle, CallbackInfo ci) {
        if (NoRender.hidesParticle(particle.getClass().getName())) ci.cancel();
    }
}


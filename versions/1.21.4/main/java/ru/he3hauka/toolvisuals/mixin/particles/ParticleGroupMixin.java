package ru.he3hauka.toolvisuals.mixin.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

@Mixin(ParticleEngine.class)
public abstract class ParticleGroupMixin {
    @Inject(method = "tickParticle", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$removeHiddenParticle(Particle particle, CallbackInfo ci) {
        if (NoRender.hidesParticle(particle.getClass().getName())) {
            particle.remove();
            ci.cancel();
        }
    }
}


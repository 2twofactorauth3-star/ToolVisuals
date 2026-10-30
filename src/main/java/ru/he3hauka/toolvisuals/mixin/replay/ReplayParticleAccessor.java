package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParticleEngine.class)
public interface ReplayParticleAccessor {
    @Accessor("resourceManager") ParticleResources toolvisuals$replayResources();
}

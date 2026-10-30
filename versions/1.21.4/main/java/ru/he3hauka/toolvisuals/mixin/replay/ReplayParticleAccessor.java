package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParticleEngine.class)
public interface ReplayParticleAccessor {
    @Accessor("textureManager") TextureManager toolvisuals$replayResources();
    @Accessor("spriteSets") Map<ResourceLocation,?> toolvisuals$spriteSets();
    @Invoker("clearParticles") void toolvisuals$clearReplayParticles();
}

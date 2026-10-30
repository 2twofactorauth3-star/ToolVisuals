package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface ReplayMinecraftAccessor {
    @Mutable @Accessor("levelExtractor") void toolvisuals$replayExtractor(LevelExtractor value);
    @Mutable @Accessor("levelRenderer") void toolvisuals$replayRenderer(LevelRenderer value);
    @Mutable @Accessor("particleEngine") void toolvisuals$replayParticles(ParticleEngine value);
    @Mutable @Accessor("gameRenderer") void toolvisuals$replayGameRenderer(GameRenderer value);
    @Mutable @Accessor("gui") void toolvisuals$replayGui(Gui value);
}

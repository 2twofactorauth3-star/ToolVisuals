package ru.he3hauka.toolvisuals.client.replay.playback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import ru.he3hauka.toolvisuals.mixin.replay.ReplayMinecraftAccessor;

final class ReplaySceneState {
    final ClientLevel level;
    final LocalPlayer player;
    final MultiPlayerGameMode mode;
    final LevelExtractor extractor;
    final LevelRenderer renderer;
    final ParticleEngine particles;
    final GameRenderer game;
    final Gui gui;
    final HitResult hit;
    final Entity picked;

    ReplaySceneState(Minecraft client) {
        this(client.level, client.player, client.gameMode, client.levelExtractor, client.levelRenderer,
                client.particleEngine, client.gameRenderer, client.gui, client.hitResult, client.crosshairPickEntity);
    }

    ReplaySceneState(ClientLevel level, LocalPlayer player, MultiPlayerGameMode mode, LevelExtractor extractor,
                     LevelRenderer renderer, ParticleEngine particles, GameRenderer game, Gui gui, HitResult hit, Entity picked) {
        this.level = level; this.player = player; this.mode = mode; this.extractor = extractor; this.renderer = renderer;
        this.particles = particles; this.game = game; this.gui = gui; this.hit = hit; this.picked = picked;
    }

    void install(Minecraft client) {
        var access = (ReplayMinecraftAccessor)client;
        access.toolvisuals$replayExtractor(extractor); access.toolvisuals$replayRenderer(renderer);
        access.toolvisuals$replayParticles(particles); access.toolvisuals$replayGameRenderer(game); access.toolvisuals$replayGui(gui);
        client.level = level; client.player = player; client.gameMode = mode; client.hitResult = hit; client.crosshairPickEntity = picked;
    }
}

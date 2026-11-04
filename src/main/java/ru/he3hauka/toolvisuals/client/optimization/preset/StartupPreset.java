package ru.he3hauka.toolvisuals.client.optimization.preset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

public final class StartupPreset {
    private static final Logger LOGGER=LoggerFactory.getLogger("ToolVisuals");
    private final boolean existingClient;
    private final String version;
    private final Path historyPath;
    private static boolean applying;
    public static boolean applying(){return applying;}

    public StartupPreset(Path configDirectory) {
        existingClient=Files.exists(configDirectory.resolve("toolvisuals.json"));
        version=FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion().getFriendlyString();
        historyPath=configDirectory.resolve("toolvisuals/preset-history.json");
    }

    public void apply(Minecraft client) {
        try {
            var history=new PresetHistory(historyPath);
            if(history.recorded(version)) return;
            if(!history.needsPreset(version,existingClient)) { history.record(version,false);return; }
            var sodium=SodiumClientMod.options();
            if(sodium.isReadOnly()) { LOGGER.warn("Performance preset skipped: Sodium configuration is read-only");return; }
            var options=client.options;
            boolean seeded=Files.isRegularFile(client.gameDirectory.toPath().resolve(".toollauncher/default-options.json"));
            int renderDistance=seeded?options.renderDistance().get():6,simulationDistance=seeded?options.simulationDistance().get():4;
            applying=true;
            StartupQuality.apply(options);
            options.renderDistance().set(renderDistance);
            options.simulationDistance().set(simulationDistance);
            options.cloudStatus().set(CloudStatus.FAST);
            options.entityShadows().set(false);
            options.particles().set(ParticleStatus.MINIMAL);
            options.biomeBlendRadius().set(0);
            options.enableVsync().set(false);
            options.framerateLimit().set(Options.UNLIMITED_FRAMERATE_CUTOFF);
            sodium.performance.animateOnlyVisibleTextures=true;
            sodium.performance.useEntityCulling=true;
            sodium.performance.useFogOcclusion=true;
            sodium.performance.useBlockFaceCulling=true;
            sodium.performance.chunkBuilderThreads=0;
            SodiumOptions.writeToDisk(sodium);
            options.save();
            if(!Files.isRegularFile(options.getFile().toPath())||Files.size(options.getFile().toPath())==0)
                throw new IOException("Minecraft options were not written");
            history.record(version,true);
            LOGGER.info("Applied the first-launch Minecraft/Sodium performance preset for {}",version);
        } catch(IOException|RuntimeException error) {
            ClientErrors.report("Apply first-launch performance preset; configuration history preserved",error);
        } finally {applying=false;}
    }
}

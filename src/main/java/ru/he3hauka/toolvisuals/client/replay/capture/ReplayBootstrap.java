package ru.he3hauka.toolvisuals.client.replay.capture;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import ru.he3hauka.toolvisuals.mixin.replay.ReplayTagsInvoker;

public final class ReplayBootstrap {
    private ReplayBootstrap() {}

    public static void capture(Minecraft client, ReplayJoinState joined, Consumer<Packet<?>> configuration,
                               Consumer<Packet<?>> play, Runnable self) {
        if (!client.isSameThread() || joined.listener != client.getConnection() || client.level == null || client.player == null)
            throw new IllegalStateException("Replay snapshot requires the current client world");
        var registry = joined.listener.registryAccess();
        configuration.accept(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(joined.listener.enabledFeatures())));
        RegistrySynchronization.packRegistries(RegistryOps.create(NbtOps.INSTANCE, registry), registry, Set.of(),
                (key, entries) -> configuration.accept(new ClientboundRegistryDataPacket(key, entries)));
        Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = new HashMap<>();
        registry.registries().forEach(entry -> tags.put(entry.key(), ReplayTagsInvoker.toolvisuals$replayTags(entry.value())));
        configuration.accept(new ClientboundUpdateTagsPacket(tags));
        configuration.accept(ClientboundFinishConfigurationPacket.INSTANCE);
        play.accept(joined.login);
        if (joined.respawn != null) play.accept(joined.respawn);
        var level = client.level;
        var player = client.player;
        var center = player.chunkPosition();
        int centerX = center.getMinBlockX() >> 4, centerZ = center.getMinBlockZ() >> 4;
        play.accept(new ClientboundSetChunkCacheRadiusPacket(64));
        play.accept(new ClientboundSetChunkCacheCenterPacket(centerX, centerZ));
        play.accept(new ClientboundChangeDifficultyPacket(level.getDifficulty(), level.getLevelData().isDifficultyLocked()));
        play.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, client.gameMode.getPlayerMode().getId()));
        play.accept(new ClientboundGameEventPacket(level.isRaining() ? ClientboundGameEventPacket.START_RAINING : ClientboundGameEventPacket.STOP_RAINING, 0));
        play.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, level.getRainLevel(1)));
        play.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, level.getThunderLevel(1)));
        play.accept(new ClientboundInitializeBorderPacket(level.getWorldBorder()));
        ReplayWorldPackets.capture(client, play);
        play.accept(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
        play.accept(ClientboundPlayerPositionPacket.of(0, net.minecraft.world.entity.PositionMoveRotation.of(player), Set.of()));
        play.accept(new ClientboundSetHealthPacket(player.getHealth(), player.getFoodData().getFoodLevel(), player.getFoodData().getSaturationLevel()));
        play.accept(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        play.accept(new ClientboundContainerSetContentPacket(player.inventoryMenu.containerId, player.inventoryMenu.getStateId(),
                player.inventoryMenu.getItems(), player.inventoryMenu.getCarried()));
        ReplayEntities.players(joined.listener, play);
        int count = 0;
        for (int x = centerX - 67; x <= centerX + 67; x++) {
            for (int z = centerZ - 67; z <= centerZ + 67; z++) {
                var chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                if (++count > 8192) throw new IllegalStateException("Replay snapshot exceeds the loaded chunk budget");
                play.accept(new ClientboundLevelChunkWithLightPacket(chunk, level.getChunkSource().getLightEngine(), null, null));
            }
        }
        self.run();
        ReplayEntities.capture(level, player, play);
        for (var team : level.getScoreboard().getPlayerTeams()) play.accept(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
    }

}

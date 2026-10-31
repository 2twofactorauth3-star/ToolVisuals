package ru.he3hauka.toolvisuals.client.replay.capture;

import java.util.HashMap;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.world.clock.ClockNetworkState;
import net.minecraft.world.clock.WorldClock;

public final class ReplayWorldPackets {
    private ReplayWorldPackets() {}

    public static void capture(Minecraft client, Consumer<Packet<?>> output) {
        var clocks = new HashMap<Holder<WorldClock>, ClockNetworkState>();
        client.level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).listElements().forEach(holder -> {
            var clock = client.getConnection().clockManager().getInstance(holder);
            clocks.put(holder, new ClockNetworkState(clock.totalTicks(), clock.partialTick(), clock.rate()));
        });
        output.accept(new ClientboundSetTimePacket(client.level.getGameTime(), clocks));
        output.accept(new ClientboundSetDefaultSpawnPositionPacket(client.level.getLevelData().getRespawnData()));
    }
}

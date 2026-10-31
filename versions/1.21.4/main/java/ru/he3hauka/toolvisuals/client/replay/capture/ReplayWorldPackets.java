package ru.he3hauka.toolvisuals.client.replay.capture;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import ru.he3hauka.toolvisuals.mixin.replay.ReplayDayTimeAccessor;

public final class ReplayWorldPackets {
    private ReplayWorldPackets() {}

    public static void capture(Minecraft client, Consumer<Packet<?>> output) {
        output.accept(new ClientboundSetTimePacket(client.level.getGameTime(), client.level.getDayTime(),
                ((ReplayDayTimeAccessor) client.level).toolvisuals$replayTickDayTime()));
        output.accept(new ClientboundSetDefaultSpawnPositionPacket(client.level.getSharedSpawnPos(), client.level.getSharedSpawnAngle()));
    }
}

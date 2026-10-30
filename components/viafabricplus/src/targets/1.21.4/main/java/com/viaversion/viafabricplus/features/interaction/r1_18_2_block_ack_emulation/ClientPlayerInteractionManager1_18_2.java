package com.viaversion.viafabricplus.features.interaction.r1_18_2_block_ack_emulation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.apache.commons.lang3.tuple.Pair;

public final class ClientPlayerInteractionManager1_18_2 {

    private final Object2ObjectLinkedOpenHashMap<Pair<BlockPos, PlayerActionC2SPacket.Action>, Pair<Vec3d, Vec2f>> unAckedActions = new Object2ObjectLinkedOpenHashMap<>();

    public void trackPlayerAction(final PlayerActionC2SPacket.Action action, final BlockPos blockPos) {
        final ClientPlayerEntity player = MinecraftClient.getInstance().player;

        final Vec2f rotation;
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_1)) {
            rotation = null;
        } else {
            rotation = new Vec2f(player.getYaw(), player.getPitch());
        }
        unAckedActions.put(Pair.of(blockPos, action), Pair.of(player.getPos(), rotation));
    }

    public void handleBlockBreakAck(final BlockPos blockPos, final BlockState expectedState, final PlayerActionC2SPacket.Action action, final boolean allGood) {
        final ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }

        final ClientWorld world = MinecraftClient.getInstance().getNetworkHandler().getWorld();

        final Pair<Vec3d, Vec2f> oldPlayerState = unAckedActions.remove(Pair.of(blockPos, action));
        final BlockState actualState = world.getBlockState(blockPos);

        if ((oldPlayerState == null || !allGood || action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK && actualState != expectedState) && (actualState != expectedState || ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2))) {
            world.setBlockState(blockPos, expectedState, Block.NOTIFY_ALL | Block.FORCE_STATE);
            if (oldPlayerState != null && (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_1) || (world == player.getWorld() && player.collidesWithStateAtPos(blockPos, expectedState)))) {
                final Vec3d oldPlayerPosition = oldPlayerState.getKey();
                if (oldPlayerState.getValue() != null) {
                    player.updatePositionAndAngles(oldPlayerPosition.x, oldPlayerPosition.y, oldPlayerPosition.z, oldPlayerState.getValue().x, oldPlayerState.getValue().y);
                } else {
                    player.updatePosition(oldPlayerPosition.x, oldPlayerPosition.y, oldPlayerPosition.z);
                }
            }
        }

        while (unAckedActions.size() >= 50) {
            ViaFabricPlusImpl.INSTANCE.logger().warn("Too many unacked block actions, dropping {}", unAckedActions.firstKey());
            unAckedActions.removeFirst();
        }
    }

}

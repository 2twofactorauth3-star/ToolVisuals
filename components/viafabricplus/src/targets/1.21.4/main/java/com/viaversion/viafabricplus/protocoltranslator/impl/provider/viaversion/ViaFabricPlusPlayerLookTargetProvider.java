package com.viaversion.viafabricplus.protocoltranslator.impl.provider.viaversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.PlayerLookTargetProvider;

public final class ViaFabricPlusPlayerLookTargetProvider extends PlayerLookTargetProvider {

    @Override
    public BlockPosition getPlayerLookTarget(UserConnection info) {
        if (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult blockHitResult) {
            final BlockPos pos = blockHitResult.getBlockPos();
            return new BlockPosition(pos.getX(), pos.getY(), pos.getZ());
        } else {
            return null;
        }
    }

}

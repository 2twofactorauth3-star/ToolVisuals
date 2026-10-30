package com.viaversion.viafabricplus.features.world.disable_sequencing;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;


public final class PendingUpdateManager1_18_2 extends PendingUpdateManager {

    @Override
    public void addPendingUpdate(BlockPos pos, BlockState state, ClientPlayerEntity player) {
    }

    @Override
    public boolean hasPendingUpdate(BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void processPendingUpdates(int maxProcessableSequence, ClientWorld world) {
    }

    @Override
    public PendingUpdateManager incrementSequence() {
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public int getSequence() {
        return 0;
    }

    @Override
    public boolean hasPendingSequence() {
        return false;
    }

}

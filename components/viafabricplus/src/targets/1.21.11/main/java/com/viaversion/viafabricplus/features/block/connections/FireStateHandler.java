package com.viaversion.viafabricplus.features.block.connections;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;


public final class FireStateHandler implements IBlockStateHandler {

    @Override
    public BlockState connect(final BlockState blockState, final LevelReader levelReader, final BlockPos blockPos) {

        final boolean canBurn = !levelReader.getBlockState(blockPos.below()).isSolidRender() && !((FireBlock) Blocks.FIRE).canBurn(levelReader.getBlockState(blockPos.below()));
        if (canBurn) {
            final FireBlock fireBlock = (FireBlock) blockState.getBlock();
            return blockState.setValue(FireBlock.NORTH, fireBlock.canBurn(levelReader.getBlockState(blockPos.north())))
                .setValue(FireBlock.EAST, fireBlock.canBurn(levelReader.getBlockState(blockPos.east())))
                .setValue(FireBlock.SOUTH, fireBlock.canBurn(levelReader.getBlockState(blockPos.south())))
                .setValue(FireBlock.WEST, fireBlock.canBurn(levelReader.getBlockState(blockPos.west())))
                .setValue(FireBlock.UP, fireBlock.canBurn(levelReader.getBlockState(blockPos.above())));
        }

        return blockState;
    }

}

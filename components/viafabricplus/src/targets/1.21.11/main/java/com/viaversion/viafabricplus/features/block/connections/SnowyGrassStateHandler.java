package com.viaversion.viafabricplus.features.block.connections;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;


public final class SnowyGrassStateHandler implements IBlockStateHandler {

    @Override
    public BlockState connect(final BlockState blockState, final LevelReader levelReader, final BlockPos blockPos) {
        return blockState.setValue(SnowyDirtBlock.SNOWY, levelReader.getBlockState(blockPos.above()).is(BlockTags.SNOW));
    }

}

package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;

import com.viaversion.viafabricplus.injection.access.block.shape.ICrossCollisionBlock;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CrossCollisionBlock.class)
public abstract class MixinCrossCollisionBlock implements ICrossCollisionBlock {

    @Unique
    private final Object2IntMap<BlockState> viaFabricPlus$SHAPE_INDEX_CACHE = new Object2IntOpenHashMap<>();

    @Override
    public int viaFabricPlus$getShapeIndex(final BlockState blockState) {
        return viaFabricPlus$SHAPE_INDEX_CACHE.computeIfAbsent(blockState, statex -> {
            int index = 0;
            if (blockState.getValue(CrossCollisionBlock.NORTH)) {
                index |= 1 << Direction.NORTH.get2DDataValue();
            }
            if (blockState.getValue(CrossCollisionBlock.EAST)) {
                index |= 1 << Direction.EAST.get2DDataValue();
            }
            if (blockState.getValue(CrossCollisionBlock.SOUTH)) {
                index |= 1 << Direction.SOUTH.get2DDataValue();
            }
            if (blockState.getValue(CrossCollisionBlock.WEST)) {
                index |= 1 << Direction.WEST.get2DDataValue();
            }

            return index;
        });
    }

}

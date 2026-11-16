package ru.he3hauka.toolvisuals.client.optimization.blockentity;

import java.util.function.Supplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService;

public final class ChestPreparation {
    private final PreparationCache<NeighborCombineResult<? extends ChestBlockEntity>> cache=new PreparationCache<>();

    public NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState state,Level level,boolean ignoreBlocked,Supplier<NeighborCombineResult<? extends ChestBlockEntity>> source) {
        long now=level.getGameTime(),version=OcclusionService.revision();
        Diagnostics.count(cache.matches(level,state,now,version,ignoreBlocked)?Diagnostics.Counter.CHEST_CACHE_HIT:Diagnostics.Counter.CHEST_CACHE_MISS,1);
        return cache.get(level,state,now,version,ignoreBlocked,source);
    }
    @SuppressWarnings("unchecked")
    public Object derived(NeighborCombineResult<?> source,int index,Supplier<Object> factory){
        return cache.derive((NeighborCombineResult<? extends ChestBlockEntity>)source,index,factory);
    }
}

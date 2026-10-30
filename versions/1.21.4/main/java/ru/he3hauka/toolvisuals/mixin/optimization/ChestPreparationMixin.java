package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.blockentity.PreparedChest;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_STATIC_BLOCK_ENTITIES;

@Mixin(ChestRenderer.class)
public abstract class ChestPreparationMixin {
    @WrapOperation(method="render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/AbstractChestBlock;combine(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/world/level/block/DoubleBlockCombiner$NeighborCombineResult;"))
    private NeighborCombineResult<? extends ChestBlockEntity> toolvisuals$cachedParts(AbstractChestBlock<?> block,BlockState state,Level level,BlockPos position,boolean ignoreBlocked,
            Operation<NeighborCombineResult<? extends ChestBlockEntity>> original,@Local(argsOnly=true) BlockEntity entity) {
        if(Optimizations.enabled(OPT_STATIC_BLOCK_ENTITIES)&&entity.getClass()==ChestBlockEntity.class&&block.getClass()==net.minecraft.world.level.block.ChestBlock.class)
            return ((PreparedChest)entity).toolvisuals$preparation().combine(state,level,ignoreBlocked,()->original.call(block,state,level,position,ignoreBlocked));
        return original.call(block,state,level,position,ignoreBlocked);
    }
    @WrapOperation(method="render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/DoubleBlockCombiner$NeighborCombineResult;apply(Lnet/minecraft/world/level/block/DoubleBlockCombiner$Combiner;)Ljava/lang/Object;",ordinal=0))
    private Object toolvisuals$openness(NeighborCombineResult<?> source,net.minecraft.world.level.block.DoubleBlockCombiner.Combiner<?,?> combiner,Operation<Object> original,@Local(argsOnly=true) BlockEntity entity){
        return toolvisuals$derived(source,combiner,original,entity,0);
    }
    @WrapOperation(method="render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/DoubleBlockCombiner$NeighborCombineResult;apply(Lnet/minecraft/world/level/block/DoubleBlockCombiner$Combiner;)Ljava/lang/Object;",ordinal=1))
    private Object toolvisuals$brightness(NeighborCombineResult<?> source,net.minecraft.world.level.block.DoubleBlockCombiner.Combiner<?,?> combiner,Operation<Object> original,@Local(argsOnly=true) BlockEntity entity){
        return toolvisuals$derived(source,combiner,original,entity,1);
    }
    private static Object toolvisuals$derived(NeighborCombineResult<?> source,net.minecraft.world.level.block.DoubleBlockCombiner.Combiner<?,?> combiner,Operation<Object> original,BlockEntity entity,int index){
        if(Optimizations.enabled(OPT_STATIC_BLOCK_ENTITIES)&&entity.getClass()==ChestBlockEntity.class)
            return ((PreparedChest)entity).toolvisuals$preparation().derived(source,index,()->original.call(source,combiner));
        return original.call(source,combiner);
    }
}

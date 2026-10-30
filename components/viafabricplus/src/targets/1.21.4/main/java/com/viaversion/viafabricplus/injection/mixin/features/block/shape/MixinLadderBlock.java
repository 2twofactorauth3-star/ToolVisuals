package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LadderBlock.class)
public abstract class MixinLadderBlock {

    @Unique
    private static final VoxelShape viaFabricPlus$east_shape_r1_8_x = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$west_shape_r1_8_x = Block.createCuboidShape(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$south_shape_r1_8_x = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$north_shape_r1_8_x = Block.createCuboidShape(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void changeOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            switch (state.get(LadderBlock.FACING)) {
                case NORTH -> ci.setReturnValue(viaFabricPlus$north_shape_r1_8_x);
                case SOUTH -> ci.setReturnValue(viaFabricPlus$south_shape_r1_8_x);
                case WEST -> ci.setReturnValue(viaFabricPlus$west_shape_r1_8_x);
                default -> ci.setReturnValue(viaFabricPlus$east_shape_r1_8_x);
            }
        }
    }

}

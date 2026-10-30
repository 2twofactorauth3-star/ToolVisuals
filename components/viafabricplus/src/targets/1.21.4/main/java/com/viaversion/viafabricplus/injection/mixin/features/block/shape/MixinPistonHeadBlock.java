package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonHeadBlock.class)
public abstract class MixinPistonHeadBlock extends FacingBlock {

    @Shadow
    @Final
    protected static VoxelShape DOWN_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape UP_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape NORTH_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SOUTH_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape WEST_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape EAST_HEAD_SHAPE;

    @Unique
    private static final VoxelShape viaFabricPlus$up_arm_shape_r1_8_x = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 12.0, 10.0);

    @Unique
    private static final VoxelShape viaFabricPlus$down_arm_shape_r1_8_x = Block.createCuboidShape(6.0, 4.0, 6.0, 10.0, 16.0, 10.0);

    @Unique
    private static final VoxelShape viaFabricPlus$south_arm_shape_r1_8_x = Block.createCuboidShape(4.0, 6.0, 0.0, 12.0, 10.0, 12.0);

    @Unique
    private static final VoxelShape viaFabricPlus$north_arm_shape_r1_8_x = Block.createCuboidShape(4.0, 6.0, 4.0, 12.0, 10.0, 16.0);

    @Unique
    private static final VoxelShape viaFabricPlus$east_arm_shape_r1_8_x = Block.createCuboidShape(0.0, 6.0, 4.0, 12.0, 10.0, 12.0);

    @Unique
    private static final VoxelShape viaFabricPlus$west_arm_shape_r1_8_x = Block.createCuboidShape(6.0, 4.0, 4.0, 10.0, 12.0, 16.0);

    @Unique
    private boolean viaFabricPlus$selfInflicted = false;

    protected MixinPistonHeadBlock(Settings settings) {
        super(settings);
    }

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void changeOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (viaFabricPlus$selfInflicted) {
            viaFabricPlus$selfInflicted = false;
            return;
        }

        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(switch (state.get(PistonHeadBlock.FACING)) {
                case DOWN -> DOWN_HEAD_SHAPE;
                case UP -> UP_HEAD_SHAPE;
                case NORTH -> NORTH_HEAD_SHAPE;
                case SOUTH -> SOUTH_HEAD_SHAPE;
                case WEST -> WEST_HEAD_SHAPE;
                case EAST -> EAST_HEAD_SHAPE;
            });
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return switch (state.get(PistonHeadBlock.FACING)) {
                case DOWN -> VoxelShapes.union(DOWN_HEAD_SHAPE, viaFabricPlus$down_arm_shape_r1_8_x);
                case UP -> VoxelShapes.union(UP_HEAD_SHAPE, viaFabricPlus$up_arm_shape_r1_8_x);
                case NORTH -> VoxelShapes.union(NORTH_HEAD_SHAPE, viaFabricPlus$north_arm_shape_r1_8_x);
                case SOUTH -> VoxelShapes.union(SOUTH_HEAD_SHAPE, viaFabricPlus$south_arm_shape_r1_8_x);
                case WEST -> VoxelShapes.union(WEST_HEAD_SHAPE, viaFabricPlus$west_arm_shape_r1_8_x);
                case EAST -> VoxelShapes.union(EAST_HEAD_SHAPE, viaFabricPlus$east_arm_shape_r1_8_x);
            };
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {

            viaFabricPlus$selfInflicted = true;
            return getOutlineShape(state, world, pos, context);
        } else {
            return super.getCollisionShape(state, world, pos, context);
        }
    }

}

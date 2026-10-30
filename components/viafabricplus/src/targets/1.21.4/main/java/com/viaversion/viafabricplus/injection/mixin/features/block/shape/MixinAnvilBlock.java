package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import com.viaversion.viafabricplus.injection.ViaFabricPlusMixinPlugin;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilBlock.class)
public abstract class MixinAnvilBlock extends FallingBlock {

    @Unique
    private static final VoxelShape viaFabricPlus$x_axis_shape_r1_12_2 = Block.createCuboidShape(0.0D, 0.0D, 2.0D, 16.0D, 16.0D, 14.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$z_axis_shape_r1_12_2 = Block.createCuboidShape(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 16.0D);

    @Shadow
    @Final
    public static EnumProperty<Direction> FACING;

    public MixinAnvilBlock(Settings settings) {
        super(settings);
    }

    @Unique
    private boolean viaFabricPlus$requireOriginalShape;

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void changeOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (ViaFabricPlusMixinPlugin.MORE_CULLING_PRESENT && viaFabricPlus$requireOriginalShape) {
            viaFabricPlus$requireOriginalShape = false;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(state.get(FACING).getAxis() == Direction.Axis.X ? viaFabricPlus$x_axis_shape_r1_12_2 : viaFabricPlus$z_axis_shape_r1_12_2);
        }
    }

    @Override
    protected VoxelShape getCullingShape(BlockState state) {

        viaFabricPlus$requireOriginalShape = true;
        return super.getCullingShape(state);
    }

}

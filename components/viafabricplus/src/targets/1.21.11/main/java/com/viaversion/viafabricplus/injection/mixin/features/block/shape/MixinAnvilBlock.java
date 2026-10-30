package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

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
    private static final VoxelShape viaFabricPlus$x_axis_shape_r1_12_2 = Block.box(0.0D, 0.0D, 2.0D, 16.0D, 16.0D, 14.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$z_axis_shape_r1_12_2 = Block.box(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 16.0D);

    @Shadow
    @Final
    public static EnumProperty<Direction> FACING;

    public MixinAnvilBlock(Properties settings) {
        super(settings);
    }

    @Unique
    private boolean viaFabricPlus$requireOriginalShape;

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void changeOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (ViaFabricPlusMixinPlugin.MORE_CULLING_PRESENT && viaFabricPlus$requireOriginalShape) {
            viaFabricPlus$requireOriginalShape = false;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(state.getValue(FACING).getAxis() == Direction.Axis.X ? viaFabricPlus$x_axis_shape_r1_12_2 : viaFabricPlus$z_axis_shape_r1_12_2);
        }
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {

        viaFabricPlus$requireOriginalShape = true;
        return super.getOcclusionShape(state);
    }

}

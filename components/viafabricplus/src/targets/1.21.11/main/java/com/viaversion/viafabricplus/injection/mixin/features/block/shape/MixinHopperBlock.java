package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.injection.ViaFabricPlusMixinPlugin;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlock.class)
public abstract class MixinHopperBlock extends BaseEntityBlock {

    @Unique
    private static final VoxelShape viaFabricPlus$inside_shape_r1_12_2 = Block.box(2.0D, 10.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$hopper_shape_r1_12_2 = Shapes.join(Shapes.block(), viaFabricPlus$inside_shape_r1_12_2, BooleanOp.ONLY_FIRST);

    @Unique
    private boolean viaFabricPlus$requireOriginalShape;

    public MixinHopperBlock(Properties settings) {
        super(settings);
    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void changeOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (ViaFabricPlusMixinPlugin.MORE_CULLING_PRESENT && viaFabricPlus$requireOriginalShape) {
            viaFabricPlus$requireOriginalShape = false;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(viaFabricPlus$hopper_shape_r1_12_2);
        }
    }

    @Inject(method = "getInteractionShape", at = @At("HEAD"), cancellable = true)
    private void changeRaycastShape(BlockState state, BlockGetter world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(viaFabricPlus$inside_shape_r1_12_2);
        }
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state) {

        viaFabricPlus$requireOriginalShape = true;
        return super.getOcclusionShape(state);
    }

}

package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import com.viaversion.viafabricplus.injection.ViaFabricPlusMixinPlugin;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class MixinBedBlock extends HorizontalFacingBlock {

    @Unique
    private static final VoxelShape viaFabricPlus$shape_r1_13_2 = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D);

    protected MixinBedBlock(Settings settings) {
        super(settings);
    }

    @Unique
    private boolean viaFabricPlus$requireOriginalShape;

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void changeOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (ViaFabricPlusMixinPlugin.MORE_CULLING_PRESENT && viaFabricPlus$requireOriginalShape) {
            viaFabricPlus$requireOriginalShape = false;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            cir.setReturnValue(viaFabricPlus$shape_r1_13_2);
        }
    }

    @Override
    public VoxelShape getCullingShape(BlockState state) {

        viaFabricPlus$requireOriginalShape = true;
        return super.getCullingShape(state);
    }

}

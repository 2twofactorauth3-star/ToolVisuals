package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import com.viaversion.viafabricplus.injection.ViaFabricPlusMixinPlugin;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CauldronBlock.class)
public abstract class MixinCauldronBlock extends AbstractCauldronBlock {

    @Unique
    private static final VoxelShape viaFabricPlus$shape_r1_12_2 = VoxelShapes.combineAndSimplify(
            VoxelShapes.fullCube(),
            Block.createCuboidShape(2.0D, 5.0D, 2.0D, 14.0D, 16.0D, 14.0D),
            BooleanBiFunction.ONLY_FIRST
    );

    public MixinCauldronBlock(Settings settings, CauldronBehavior.CauldronBehaviorMap behaviorMap) {
        super(settings, behaviorMap);
    }

    @Unique
    private boolean viaFabricPlus$requireOriginalShape;

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (ViaFabricPlusMixinPlugin.MORE_CULLING_PRESENT && viaFabricPlus$requireOriginalShape) {
            viaFabricPlus$requireOriginalShape = false;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            return viaFabricPlus$shape_r1_12_2;
        }
        return super.getOutlineShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state) {

        viaFabricPlus$requireOriginalShape = true;
        return super.getCullingShape(state);
    }

}

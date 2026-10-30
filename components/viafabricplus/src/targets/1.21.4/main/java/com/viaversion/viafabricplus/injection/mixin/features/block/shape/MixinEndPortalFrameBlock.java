package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.BooleanProperty;
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

@Mixin(EndPortalFrameBlock.class)
public abstract class MixinEndPortalFrameBlock extends Block {

    @Shadow
    @Final
    protected static VoxelShape FRAME_SHAPE;

    @Shadow
    @Final
    public static BooleanProperty EYE;

    @Unique
    private static final VoxelShape viaFabricPlus$eye_shape_r1_12_2 = Block.createCuboidShape(5.0D, 13.0D, 5.0D, 11.0D, 16.0D, 11.0D);

    @Unique
    private static final VoxelShape viaFabricPlus$frame_with_eye_shape_r1_12_2 = VoxelShapes.union(FRAME_SHAPE, viaFabricPlus$eye_shape_r1_12_2);

    public MixinEndPortalFrameBlock(Settings settings) {
        super(settings);
    }

    @Inject(method = "getOutlineShape", at = @At(value = "HEAD"), cancellable = true)
    private void changeOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(FRAME_SHAPE);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            return state.get(EYE) ? viaFabricPlus$frame_with_eye_shape_r1_12_2 : FRAME_SHAPE;
        } else {
            return super.getCollisionShape(state, world, pos, context);
        }
    }

}

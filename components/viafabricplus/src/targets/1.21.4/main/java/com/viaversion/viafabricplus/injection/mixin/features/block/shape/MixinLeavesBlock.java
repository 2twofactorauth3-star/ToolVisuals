package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public abstract class MixinLeavesBlock extends Block {

    public MixinLeavesBlock(Settings settings) {
        super(settings);
    }

    @Inject(method = "getSidesShape", at = @At("HEAD"), cancellable = true)
    private void changeSidesShape(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_14, ProtocolVersion.v1_15_2)) {
            cir.setReturnValue(super.getSidesShape(state, world, pos));
        }
    }

}

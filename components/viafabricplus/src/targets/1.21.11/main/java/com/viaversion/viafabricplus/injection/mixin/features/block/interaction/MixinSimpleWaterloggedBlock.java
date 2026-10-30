package com.viaversion.viafabricplus.injection.mixin.features.block.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleWaterloggedBlock.class)
public interface MixinSimpleWaterloggedBlock {

    @Inject(method = "canPlaceLiquid", at = @At("RETURN"), cancellable = true)
    private void preventLiquidPlacement1_12_2(LivingEntity livingEntity, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "placeLiquid", at = @At("HEAD"), cancellable = true)
    private void preventLiquidPlacement1_12_2(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(false);
        }
    }

}

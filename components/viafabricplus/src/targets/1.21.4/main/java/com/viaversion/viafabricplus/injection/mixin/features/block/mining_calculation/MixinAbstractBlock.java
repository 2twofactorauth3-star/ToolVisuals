package com.viaversion.viafabricplus.injection.mixin.features.block.mining_calculation;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class MixinAbstractBlock {

    @Inject(method = "calcBlockBreakingDelta", at = @At("HEAD"), cancellable = true)
    private void changeMiningSpeedCalculation(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_4_6tor1_4_7)) {
            final float hardness = state.getHardness(world, pos);
            if (hardness == -1.0F) {
                cir.setReturnValue(0.0F);
            } else {
                if (!player.canHarvest(state)) {
                    cir.setReturnValue(1.0F / hardness / 100F);
                } else {
                    cir.setReturnValue(player.getBlockBreakingSpeed(state) / hardness / 30F);
                }
            }
        }
    }

}

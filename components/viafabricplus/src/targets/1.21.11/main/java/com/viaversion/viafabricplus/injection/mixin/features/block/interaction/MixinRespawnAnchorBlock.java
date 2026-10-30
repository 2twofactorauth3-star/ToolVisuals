package com.viaversion.viafabricplus.injection.mixin.features.block.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RespawnAnchorBlock.class)
public abstract class MixinRespawnAnchorBlock {

    @Inject(method = "useWithoutItem", at = @At("RETURN"), cancellable = true)
    private void swingHand(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_9)) {
            if (cir.getReturnValue() == InteractionResult.CONSUME && !level.environmentAttributes().getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, blockPos)) {
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }

}

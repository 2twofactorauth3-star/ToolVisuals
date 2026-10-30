package com.viaversion.viafabricplus.injection.mixin.features.block.interaction;

import net.minecraft.block.BlockState;
import net.minecraft.block.DecoratedPotBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotBlock.class)
public abstract class MixinDecoratedPotBlock {

    @Redirect(method = "onUseWithItem", at = @At(value = "FIELD", target = "Lnet/minecraft/util/ActionResult;SUCCESS:Lnet/minecraft/util/ActionResult$Success;", ordinal = 0))
    private ActionResult.Success dontSwingHand() {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            return ActionResult.CONSUME;
        } else {
            return ActionResult.SUCCESS;
        }
    }

    @Inject(method = "onUseWithItem", at = @At("HEAD"), cancellable = true)
    private void alwaysPass(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_2)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

}

package com.viaversion.viafabricplus.injection.mixin.features.block.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlock.class)
public abstract class MixinSignBlock {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void changeInteractionCalculation(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!world.isClientSide()) {
            return;
        }

        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {

            cir.setReturnValue(InteractionResult.SUCCESS);
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_4)) {

            final ItemStack itemStack = player.getItemInHand(hand);
            final Item item = itemStack.getItem();
            final boolean isSuccess = (item instanceof DyeItem || itemStack.is(Items.GLOW_INK_SAC) || itemStack.is(Items.INK_SAC)) && player.mayBuild();

            cir.setReturnValue(isSuccess ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }
    }

}

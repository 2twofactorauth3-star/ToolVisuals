package com.viaversion.viafabricplus.injection.mixin.features.v26_2;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockTransformer.class)
public abstract class MixinBlockTransformer {

    @Inject(method = "playerHasBlockingItemUseIntent", at = @At("RETURN"), cancellable = true)
    private static void onlyCheckBlockingIntentForAxes(UseOnContext context, CallbackInfoReturnable<Boolean> cir) {

        if (cir.getReturnValueZ() && ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            final Holder<BlockTransformer> transformer = context.getItemInHand().get(DataComponents.BLOCK_TRANSFORMER);
            cir.setReturnValue(transformer != null && transformer.is(BlockTransformers.AXE));
        }
    }

    @Inject(method = "transformBlock", at = @At("HEAD"), cancellable = true)
    private void douseCampfires(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {

        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2) && context.getClickedFace() != Direction.DOWN) {
            final Holder<BlockTransformer> transformer = context.getItemInHand().get(DataComponents.BLOCK_TRANSFORMER);
            final BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            if (transformer != null && transformer.is(BlockTransformers.SHOVEL) && state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
                CampfireBlock.douse(context.getPlayer(), context.getLevel(), context.getClickedPos(), state);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }

}

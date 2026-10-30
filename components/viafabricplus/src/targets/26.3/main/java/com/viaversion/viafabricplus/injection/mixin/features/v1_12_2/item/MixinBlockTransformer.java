package com.viaversion.viafabricplus.injection.mixin.features.v1_12_2.item;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockTransformer.class)
public abstract class MixinBlockTransformer {

    @Inject(method = "transformBlock", at = @At("HEAD"), cancellable = true)
    private void preventAxeStripping(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            final Holder<BlockTransformer> transformer = context.getItemInHand().get(DataComponents.BLOCK_TRANSFORMER);
            if (transformer != null && transformer.is(BlockTransformers.AXE)) {
                cir.setReturnValue(InteractionResult.PASS);
            }
        }
    }

}

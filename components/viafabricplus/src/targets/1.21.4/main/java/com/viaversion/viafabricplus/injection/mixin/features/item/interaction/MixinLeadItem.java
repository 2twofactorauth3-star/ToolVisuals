package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.LeadItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeadItem.class)
public abstract class MixinLeadItem {

    @Inject(method = "useOnBlock", at = @At("RETURN"), cancellable = true)
    private void swingHand(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            if (context.getWorld().getBlockState(context.getBlockPos()).isIn(BlockTags.FENCES)) {
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }

}

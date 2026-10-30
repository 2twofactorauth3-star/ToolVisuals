package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.item.FireChargeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireChargeItem.class)
public abstract class MixinFireChargeItem {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void disableClientSideUsage(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4) && context.getWorld().isClient) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

}

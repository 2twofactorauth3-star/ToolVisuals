package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public abstract class MixinAxeItem {

    @Shadow
    private static boolean shouldCancelStripAttempt(ItemUsageContext context) {
        return false;
    }

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/AxeItem;shouldCancelStripAttempt(Lnet/minecraft/item/ItemUsageContext;)Z"))
    private boolean neverCancelStripAttempt(ItemUsageContext context) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5)) {
            return false;
        } else {
            return shouldCancelStripAttempt(context);
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void disableUse(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

}

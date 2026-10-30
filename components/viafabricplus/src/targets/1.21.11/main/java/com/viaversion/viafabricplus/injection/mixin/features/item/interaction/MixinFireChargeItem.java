package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.context.UseOnContext;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireChargeItem.class)
public abstract class MixinFireChargeItem {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void disableClientSideUsage(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4) && context.getLevel().isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

}

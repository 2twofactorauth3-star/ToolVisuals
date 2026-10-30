package com.viaversion.viafabricplus.injection.mixin.features.movement.collision;

import net.minecraft.block.HoneyBlock;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneyBlock.class)
public abstract class MixinHoneyBlock {

    @Inject(method = {"method_65067", "method_65068"}, at = @At("HEAD"), cancellable = true)
    private static void simplifyVelocityComparisons(double d, CallbackInfoReturnable<Double> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            cir.setReturnValue(d);
        }
    }

}

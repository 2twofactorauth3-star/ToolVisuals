package com.viaversion.viafabricplus.injection.mixin.features.movement.constants;

import net.minecraft.entity.Entity;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @ModifyConstant(method = "checkBlockCollision", constant = @Constant(doubleValue = 9.999999747378752E-6))
    private double fixBlockCollisionMargin(double constant) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_1)) {
            return 1E-3;
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            return 1E-7;
        } else {
            return constant;
        }
    }

    @ModifyConstant(method = "movementInputToVelocity", constant = @Constant(doubleValue = 1E-7))
    private static double fixVelocityEpsilon(double epsilon) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            return 1E-4;
        } else {
            return epsilon;
        }
    }

    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void expandHitBox(CallbackInfoReturnable<Float> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            cir.setReturnValue(0.1F);
        }
    }

}

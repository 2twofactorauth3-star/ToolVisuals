package com.viaversion.viafabricplus.injection.mixin.features.limitation.allow_negative_amplifier;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.MathHelper;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StatusEffectInstance.class)
public abstract class MixinStatusEffectInstance {

    @Redirect(method = "<init>(Lnet/minecraft/registry/entry/RegistryEntry;IIZZZLnet/minecraft/entity/effect/StatusEffectInstance;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"))
    private int dontClampValue(int value, int min, int max) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_3)) {
            return value;
        } else {
            return MathHelper.clamp(value, min, max);
        }
    }

}

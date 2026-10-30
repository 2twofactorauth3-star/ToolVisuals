package com.viaversion.viafabricplus.injection.mixin.features.entity.attribute;

import net.minecraft.entity.LivingEntity;

import com.viaversion.viafabricplus.features.entity.attribute.EnchantmentAttributesEmulation1_20_6;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "getVelocityMultiplier", at = @At("HEAD"))
    private void setGenericMovementEfficiencyAttribute(CallbackInfoReturnable<Float> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5)) {
            EnchantmentAttributesEmulation1_20_6.setGenericMovementEfficiencyAttribute((LivingEntity) (Object) this);
        }
    }

}

package com.viaversion.viafabricplus.injection.mixin.features.swinging;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @WrapWithCondition(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private boolean dontSwingHand(LivingEntity instance, InteractionHand hand) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_15_2);
    }

}

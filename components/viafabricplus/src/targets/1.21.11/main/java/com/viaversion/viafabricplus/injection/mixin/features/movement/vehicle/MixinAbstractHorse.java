package com.viaversion.viafabricplus.injection.mixin.features.movement.vehicle;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractHorse.class)
public abstract class MixinAbstractHorse {

    @WrapWithCondition(method = "addPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;absSnapRotationTo(FF)V"))
    private boolean dontSetPassengerRotation(Entity instance, float yRot, float xRot) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_21_7);
    }

}

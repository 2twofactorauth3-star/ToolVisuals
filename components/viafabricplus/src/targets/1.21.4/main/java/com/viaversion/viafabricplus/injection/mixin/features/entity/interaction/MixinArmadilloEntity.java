package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.entity.passive.ArmadilloEntity;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArmadilloEntity.class)
public abstract class MixinArmadilloEntity {

    @Redirect(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/ArmadilloEntity;isNotIdle()Z"))
    private boolean changeCondition(ArmadilloEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5)) {
            return false;
        } else {
            return instance.isNotIdle();
        }
    }

}

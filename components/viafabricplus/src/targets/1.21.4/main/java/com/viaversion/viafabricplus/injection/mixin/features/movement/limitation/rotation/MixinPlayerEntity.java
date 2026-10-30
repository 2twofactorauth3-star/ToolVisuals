package com.viaversion.viafabricplus.injection.mixin.features.movement.limitation.rotation;

import net.minecraft.entity.player.PlayerEntity;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {

    @Redirect(method = "getMaxRelativeHeadRotation", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isBlocking()Z"))
    private boolean dontModifyHeadRotationWhenBlocking(PlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_20_2) && instance.isBlocking();
    }

}

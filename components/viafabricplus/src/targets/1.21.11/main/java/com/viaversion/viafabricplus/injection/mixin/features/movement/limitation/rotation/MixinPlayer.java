package com.viaversion.viafabricplus.injection.mixin.features.movement.limitation.rotation;

import net.minecraft.world.entity.player.Player;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class MixinPlayer {

    @Redirect(method = "getMaxHeadRotationRelativeToBody", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isBlocking()Z"))
    private boolean dontModifyHeadRotationWhenBlocking(Player instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_20_2) && instance.isBlocking();
    }

}

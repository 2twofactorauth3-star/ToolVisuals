package com.viaversion.viafabricplus.injection.mixin.features.movement.constants;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;square(D)D"))
    private double changeMagnitude(double n) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18)) {
            return 9.0E-4D;
        } else {
            return Mth.square(n);
        }
    }

}

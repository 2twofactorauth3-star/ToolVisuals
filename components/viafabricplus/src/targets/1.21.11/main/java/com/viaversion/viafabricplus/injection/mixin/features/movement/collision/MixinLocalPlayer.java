package com.viaversion.viafabricplus.injection.mixin.features.movement.collision;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {

    @Inject(method = "isHorizontalCollisionMinor", at = @At("HEAD"), cancellable = true)
    private void neverCollideSoftly(Vec3 adjustedMovement, CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_17_1)) {
            cir.setReturnValue(false);
        }
    }

}

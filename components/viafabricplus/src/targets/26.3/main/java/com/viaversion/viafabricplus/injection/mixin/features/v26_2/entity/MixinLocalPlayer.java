package com.viaversion.viafabricplus.injection.mixin.features.v26_2.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z"))
    private boolean moveOutOfBlocksWhileRiding(boolean original) {

        return ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3) && original;
    }

}

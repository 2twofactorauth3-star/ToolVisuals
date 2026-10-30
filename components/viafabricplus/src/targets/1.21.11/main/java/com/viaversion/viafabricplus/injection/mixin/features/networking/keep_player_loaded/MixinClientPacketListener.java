package com.viaversion.viafabricplus.injection.mixin.features.networking.keep_player_loaded;

import net.minecraft.client.multiplayer.ClientPacketListener;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(method = "hasClientLoaded", at = @At("HEAD"), cancellable = true)
    private void alwaysLoadPlayer(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_2)) {
            cir.setReturnValue(true);
        }
    }

}

package com.viaversion.viafabricplus.injection.mixin.features.networking.srv_resolving;

import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerAddress.class)
public abstract class MixinServerAddress {

    @Shadow
    @Final
    private static ServerAddress INVALID;

    @Inject(method = "parseString", at = @At("RETURN"), cancellable = true)
    private static void resolveSrv(String address, CallbackInfoReturnable<ServerAddress> cir) {
        if (!cir.getReturnValue().equals(INVALID) && ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_4)) {
            cir.setReturnValue(ServerNameResolver.DEFAULT.redirectHandler.lookupRedirect(cir.getReturnValue()).orElse(cir.getReturnValue()));
        }
    }

}

package com.viaversion.viafabricplus.injection.mixin.features.networking.legacy_chat_signature;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.features.networking.legacy_chat_signature.KeyPairResponse1_19_0;
import com.viaversion.viafabricplus.injection.access.networking.legacy_chat_signature.ILegacyKeySignatureStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;

@Mixin(value = YggdrasilUserApiService.class, remap = false)
public abstract class MixinYggdrasilUserApiService {

    @Shadow
    @Final
    private MinecraftClient minecraftClient;

    @Shadow
    @Final
    private URL routeKeyPair;

    @Inject(method = "getKeyPair", at = @At("HEAD"), cancellable = true)
    private void storeLegacyPublicKeySignature(CallbackInfoReturnable<KeyPairResponse> cir) {
        final KeyPairResponse1_19_0 response = minecraftClient.post(routeKeyPair, KeyPairResponse1_19_0.class);

        if (response == null) {
            cir.setReturnValue(null);
            return;
        }

        final KeyPairResponse keyPairResponse = new KeyPairResponse(
                response.keyPair(),
                response.publicKeySignatureV2(),
                response.expiresAt(),
                response.refreshedAfter()
        );

        if (response.publicKeySignature() != null && response.publicKeySignature().array().length != 0) {
            ((ILegacyKeySignatureStorage) (Object) keyPairResponse).viafabricplus$setLegacyPublicKeySignature(response.publicKeySignature().array());
        } else {
            ViaFabricPlusImpl.INSTANCE.logger().error("Could not get legacy public key signature. 1.19.0 with secure-profiles enabled will not work!");
        }

        cir.setReturnValue(keyPairResponse);
    }

}

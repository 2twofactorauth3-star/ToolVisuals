package com.viaversion.viafabricplus.injection.mixin.features.networking.legacy_chat_signature;

import net.minecraft.client.session.ProfileKeysImpl;
import net.minecraft.network.encryption.PlayerPublicKey;

import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.viaversion.viafabricplus.injection.access.networking.legacy_chat_signature.ILegacyKeySignatureStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProfileKeysImpl.class)
public abstract class MixinProfileKeysImpl {

    @Inject(method = "decodeKeyPairResponse", at = @At("RETURN"))
    private static void trackLegacyKey(KeyPairResponse keyPairResponse, CallbackInfoReturnable<PlayerPublicKey.PublicKeyData> cir) {
        ((ILegacyKeySignatureStorage) (Object) cir.getReturnValue()).viafabricplus$setLegacyPublicKeySignature(((ILegacyKeySignatureStorage) (Object) keyPairResponse).viafabricplus$getLegacyPublicKeySignature());
    }

}

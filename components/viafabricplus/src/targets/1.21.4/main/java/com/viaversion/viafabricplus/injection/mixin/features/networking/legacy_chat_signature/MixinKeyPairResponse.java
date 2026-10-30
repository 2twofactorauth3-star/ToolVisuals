package com.viaversion.viafabricplus.injection.mixin.features.networking.legacy_chat_signature;

import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.viaversion.viafabricplus.injection.access.networking.legacy_chat_signature.ILegacyKeySignatureStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = KeyPairResponse.class, remap = false)
public abstract class MixinKeyPairResponse implements ILegacyKeySignatureStorage {

    @Unique
    private byte[] viaFabricPlus$legacyKeySignature;

    @Override
    public byte[] viafabricplus$getLegacyPublicKeySignature() {
        return this.viaFabricPlus$legacyKeySignature;
    }

    @Override
    public void viafabricplus$setLegacyPublicKeySignature(byte[] signature) {
        this.viaFabricPlus$legacyKeySignature = signature;
    }

}

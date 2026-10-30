package com.viaversion.viafabricplus.injection.mixin.features.networking.legacy_chat_signature;

import net.minecraft.client.multiplayer.AccountProfileKeyPairManager;
import net.minecraft.world.entity.player.ProfilePublicKey;

import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.viaversion.viafabricplus.injection.access.networking.legacy_chat_signature.IProfilePublicKey_Data;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AccountProfileKeyPairManager.class)
public abstract class MixinAccountProfileKeyPairManager {

    @Inject(method = "parsePublicKey", at = @At("RETURN"))
    private static void trackLegacyKey(KeyPairResponse keyPairResponse, CallbackInfoReturnable<ProfilePublicKey.Data> cir) {
        ((IProfilePublicKey_Data) (Object) cir.getReturnValue()).viafabricplus$setLegacyPublicKeySignature(((IProfilePublicKey_Data) (Object) keyPairResponse).viafabricplus$getLegacyPublicKeySignature());
    }

}

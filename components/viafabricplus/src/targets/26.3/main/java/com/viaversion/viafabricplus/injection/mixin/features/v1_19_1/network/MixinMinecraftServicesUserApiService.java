package com.viaversion.viafabricplus.injection.mixin.features.v1_19_1.network;

import com.mojang.authlib.HttpDiscoveryService;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.services.MinecraftServicesDiscoveryService;
import com.mojang.authlib.services.MinecraftServicesUserApiService;
import com.mojang.authlib.services.response.KeyPairResponse;
import com.mojang.authlib.services.response.discovery.Service;
import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.features.v1_19_1.KeyPairResponse1_19_0;
import com.viaversion.viafabricplus.injection.access.v1_19_1.IProfilePublicKey_Data;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MinecraftServicesUserApiService.class, remap = false)
public abstract class MixinMinecraftServicesUserApiService {

    @Shadow
    @Final
    private MinecraftClient minecraftClient;

    @Shadow
    @Final
    private MinecraftServicesDiscoveryService discoveryService;

    @Overwrite
    public KeyPairResponse getKeyPair() {
        final KeyPairResponse1_19_0 response;
        try {
            response = minecraftClient.post(HttpDiscoveryService.constantURL(discoveryService.getUrl(Service.PLAYER, "getCertificates")), KeyPairResponse1_19_0.class);
        } catch (final MinecraftClientException e) {
            return null;
        }

        if (response == null) {
            return null;
        }

        final KeyPairResponse keyPairResponse = new KeyPairResponse(
            response.keyPair(),
            response.publicKeySignatureV2(),
            response.expiresAt(),
            response.refreshedAfter()
        );

        if (response.publicKeySignature() != null && response.publicKeySignature().array().length != 0) {
            ((IProfilePublicKey_Data) (Object) keyPairResponse).viafabricplus$setLegacyPublicKeySignature(response.publicKeySignature().array());
        } else {
            ViaFabricPlusImpl.impl().logger().error("Could not get legacy public key signature. 1.19.0 with secure-profiles enabled will not work!");
        }

        return keyPairResponse;
    }

}

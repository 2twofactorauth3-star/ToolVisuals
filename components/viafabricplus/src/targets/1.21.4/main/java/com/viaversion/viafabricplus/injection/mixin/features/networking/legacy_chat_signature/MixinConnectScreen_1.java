package com.viaversion.viafabricplus.injection.mixin.features.networking.legacy_chat_signature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.injection.access.base.IClientConnection;
import com.viaversion.viafabricplus.injection.access.networking.legacy_chat_signature.ILegacyKeySignatureStorage;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession1_19_0;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession1_19_1;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.security.PrivateKey;
import java.util.UUID;

@Mixin(targets = "net.minecraft.client.gui.screen.multiplayer.ConnectScreen$1")
public abstract class MixinConnectScreen_1 {

    @Shadow
    @Final
    MinecraftClient field_33738;

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelFuture;syncUninterruptibly()Lio/netty/channel/ChannelFuture;", remap = false, shift = At.Shift.AFTER))
    private void setupChatSessions(CallbackInfo ci, @Local ClientConnection clientConnection) {
        final UserConnection connection = ((IClientConnection) clientConnection).viaFabricPlus$getUserConnection();

        if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_19, ProtocolVersion.v1_19_1)) {
            final PlayerKeyPair keyPair = MinecraftClient.getInstance().getProfileKeys().fetchKeyPair().join().orElse(null);
            if (keyPair != null) {
                final PlayerPublicKey.PublicKeyData publicKeyData = keyPair.publicKey().data();
                final PrivateKey privateKey = keyPair.privateKey();
                final long expiresAt = publicKeyData.expiresAt().toEpochMilli();
                final byte[] publicKey = publicKeyData.key().getEncoded();
                final UUID uuid = this.field_33738.getSession().getUuidOrNull();

                connection.put(new ChatSession1_19_1(uuid, privateKey, new ProfileKey(expiresAt, publicKey, publicKeyData.keySignature())));
                if (ProtocolTranslator.getTargetVersion() == ProtocolVersion.v1_19) {
                    final byte[] legacyKeySignature = ((ILegacyKeySignatureStorage) (Object) publicKeyData).viafabricplus$getLegacyPublicKeySignature();
                    if (legacyKeySignature != null) {
                        connection.put(new ChatSession1_19_0(uuid, privateKey, new ProfileKey(expiresAt, publicKey, legacyKeySignature)));
                    }
                }
            } else {
                ViaFabricPlusImpl.INSTANCE.logger().error("Could not get public key signature. Joining servers with enforce-secure-profiles enabled will not work!");
            }
        }
    }

}

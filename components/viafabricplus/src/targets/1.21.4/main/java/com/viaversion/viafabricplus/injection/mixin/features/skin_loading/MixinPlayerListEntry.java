package com.viaversion.viafabricplus.injection.mixin.features.skin_loading;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerListEntry {

    @Redirect(method = "texturesSupplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/PlayerSkinProvider;fetchSkinTextures(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<Optional<SkinTextures>> fetchGameProfileProperties(PlayerSkinProvider instance, GameProfile profile) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20) && !profile.getProperties().containsKey("textures")) {
            return CompletableFuture.supplyAsync(() -> {
                final ProfileResult profileResult = MinecraftClient.getInstance().getSessionService().fetchProfile(profile.getId(), true);
                return profileResult == null ? profile : profileResult.profile();
            }, Util.getMainWorkerExecutor()).thenCompose(instance::fetchSkinTextures);
        } else {
            return instance.fetchSkinTextures(profile);
        }
    }

}

package com.viaversion.viafabricplus.protocoltranslator.impl.provider.vialegacy;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.raphimc.vialegacy.protocol.release.r1_7_6_10tor1_8.model.GameProfile;
import net.raphimc.vialegacy.protocol.release.r1_7_6_10tor1_8.provider.GameProfileFetcher;

import java.net.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ViaFabricPlusGameProfileFetcher extends GameProfileFetcher {

    private static final HttpAuthenticationService AUTHENTICATION_SERVICE = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
    private static final MinecraftSessionService SESSION_SERVICE = AUTHENTICATION_SERVICE.createMinecraftSessionService();
    private static final GameProfileRepository GAME_PROFILE_REPOSITORY = AUTHENTICATION_SERVICE.createProfileRepository();

    @Override
    public UUID loadMojangUUID(String playerName) throws Exception {
        final CompletableFuture<com.mojang.authlib.GameProfile> future = new CompletableFuture<>();
        GAME_PROFILE_REPOSITORY.findProfilesByNames(new String[]{playerName}, new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(com.mojang.authlib.GameProfile profile) {
                future.complete(profile);
            }

            @Override
            public void onProfileLookupFailed(String profileName, Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        if (!future.isDone()) {
            future.completeExceptionally(new ProfileNotFoundException());
        }
        return future.get().getId();
    }

    @Override
    public GameProfile loadGameProfile(UUID uuid) {
        final ProfileResult result = SESSION_SERVICE.fetchProfile(uuid, true);
        if (result == null) {
            throw new ProfileNotFoundException();
        }

        final com.mojang.authlib.GameProfile authLibProfile = result.profile();
        final GameProfile mcProfile = new GameProfile(authLibProfile.getName(), authLibProfile.getId());

        for (final Map.Entry<String, Property> entry : authLibProfile.getProperties().entries()) {
            mcProfile.addProperty(new GameProfile.Property(entry.getValue().name(), entry.getValue().value(), entry.getValue().signature()));
        }
        return mcProfile;
    }

}

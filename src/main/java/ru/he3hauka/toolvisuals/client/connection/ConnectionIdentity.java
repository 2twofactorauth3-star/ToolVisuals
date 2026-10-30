package ru.he3hauka.toolvisuals.client.connection;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import com.mojang.authlib.services.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.mixin.accounts.MinecraftSessionAccessor;

public final class ConnectionIdentity {
    public static final int MAX_NAME_LENGTH=16;
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,"+MAX_NAME_LENGTH+"}");
    private static final String RANDOM_CHARACTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static User original;
    private static CompletableFuture<ProfileResult> originalProfile;
    private static ProfileKeyPairManager originalKeys;
    private ConnectionIdentity() {}
    public static boolean valid(String name) { return name != null && NAME.matcher(name).matches(); }
    public static String randomName(){
        var name=new StringBuilder("ToolVisuals_");var random=ThreadLocalRandom.current();
        while(name.length()<MAX_NAME_LENGTH)name.append(RANDOM_CHARACTERS.charAt(random.nextInt(RANDOM_CHARACTERS.length())));
        return name.toString();
    }
    public static UUID offlineId(String name) { return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)); }
    public static void initialize(Minecraft client) {
        if (original != null) return;
        original = client.getUser();
        originalProfile = ((MinecraftSessionAccessor)(Object)client).toolvisuals$profile();
        originalKeys = client.getProfileKeyPairManager();
        String saved = ToolVisualsClient.CONFIG.nickname();
        if (valid(saved)) try { apply(client, saved); }
        catch(java.io.IOException error){org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot restore saved account; original account file preserved",error);}
    }
    public static boolean custom(Minecraft client) { return original != null && client.getUser() != original; }
    public static com.mojang.authlib.GameProfile launcherProfile(Minecraft client) {
        if (original==null) return client.getGameProfile();
        var result=originalProfile.getNow(null);
        return result==null?new com.mojang.authlib.GameProfile(original.getProfileId(),original.getName()):result.profile();
    }
    public static void apply(Minecraft client, String name) throws java.io.IOException {
        if (!valid(name)) throw new IllegalArgumentException("Invalid Minecraft nickname");
        if (client.level != null || client.getConnection() != null) throw new IllegalStateException("Disconnect before changing identity");
        if (original == null) initialize(client);
        var access = (MinecraftSessionAccessor)(Object)client;
        if (name.equals(original.getName())) { restore(client); return; }
        ToolVisualsClient.ACCOUNTS.used(name);
        access.toolvisuals$setUser(new User(name, offlineId(name), "", Optional.empty(), Optional.empty()));
        access.toolvisuals$setProfile(CompletableFuture.completedFuture(null));
        access.toolvisuals$setKeys(ProfileKeyPairManager.EMPTY_KEY_MANAGER);
        ToolVisualsClient.CONFIG.setNickname(name);
    }
    public static void restore(Minecraft client) {
        if (client.level != null || client.getConnection() != null) throw new IllegalStateException("Disconnect before changing identity");
        if (original == null) return;
        var access = (MinecraftSessionAccessor)(Object)client;
        access.toolvisuals$setUser(original); access.toolvisuals$setProfile(originalProfile); access.toolvisuals$setKeys(originalKeys);
        ToolVisualsClient.CONFIG.setNickname("");
    }
}

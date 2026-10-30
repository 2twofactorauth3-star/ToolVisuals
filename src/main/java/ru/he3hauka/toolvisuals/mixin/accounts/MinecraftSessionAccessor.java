package ru.he3hauka.toolvisuals.mixin.accounts;

import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.services.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftSessionAccessor {
    @Mutable @Accessor("user") void toolvisuals$setUser(User user);
    @Accessor("profileFuture") CompletableFuture<ProfileResult> toolvisuals$profile();
    @Mutable @Accessor("profileFuture") void toolvisuals$setProfile(CompletableFuture<ProfileResult> profile);
    @Mutable @Accessor("profileKeyPairManager") void toolvisuals$setKeys(ProfileKeyPairManager keys);
}

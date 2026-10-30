package com.viaversion.viafabricplus.injection.mixin.base.connection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public final class MixinMinecraftClient {

    @Inject(method = "startIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/lang/String;ILnet/minecraft/network/listener/ClientLoginPacketListener;)V"))
    private void disableProtocolTranslator(LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci, @Local ClientConnection clientConnection) {
        ProtocolTranslator.setTargetVersion(ProtocolTranslator.NATIVE_VERSION, true);
        ProtocolTranslator.injectPreviousVersionReset(clientConnection.channel);
    }

}

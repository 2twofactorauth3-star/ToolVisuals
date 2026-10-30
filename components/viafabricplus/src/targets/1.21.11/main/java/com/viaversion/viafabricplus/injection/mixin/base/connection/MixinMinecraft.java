package com.viaversion.viafabricplus.injection.mixin.base.connection;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "doWorldLoad", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;initiateServerboundPlayConnection(Ljava/lang/String;ILnet/minecraft/network/protocol/login/ClientLoginPacketListener;)V"))
    private void disableProtocolTranslator(LevelStorageSource.LevelStorageAccess session, PackRepository dataPackManager, WorldStem saveLoader, boolean newWorld, CallbackInfo ci, @Local Connection clientConnection) {
        ProtocolTranslator.setTargetVersion(ProtocolTranslator.NATIVE_VERSION, true);
        if (clientConnection.isConnected()) {
            ProtocolTranslator.injectPreviousVersionReset(clientConnection.channel);
        } else {
            clientConnection.pendingActions.add(connection -> ProtocolTranslator.injectPreviousVersionReset(connection.channel));
        }
    }

}

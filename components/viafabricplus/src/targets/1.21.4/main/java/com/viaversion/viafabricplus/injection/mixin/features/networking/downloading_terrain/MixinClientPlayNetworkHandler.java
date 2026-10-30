package com.viaversion.viafabricplus.injection.mixin.features.networking.downloading_terrain;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;

import com.viaversion.viafabricplus.injection.access.networking.downloading_terrain.IDownloadingTerrainScreen;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "onPlayerSpawnPosition", at = @At("RETURN"))
    private void moveDownloadingTerrainClosing(PlayerSpawnPositionS2CPacket packet, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_18_2, ProtocolVersion.v1_20_2) && this.client.currentScreen instanceof IDownloadingTerrainScreen mixinDownloadingTerrainScreen) {
            mixinDownloadingTerrainScreen.viaFabricPlus$setReady();
        }
    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    private void closeDownloadingTerrain(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18) && this.client.currentScreen instanceof IDownloadingTerrainScreen mixinDownloadingTerrainScreen) {
            mixinDownloadingTerrainScreen.viaFabricPlus$setReady();
        }
    }

}

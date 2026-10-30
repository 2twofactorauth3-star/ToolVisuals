package com.viaversion.viafabricplus.injection.mixin.features.networking.level_loading;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;

import com.viaversion.viafabricplus.injection.access.networking.downloading_terrain.ILevelLoadingScreen;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener extends ClientCommonPacketListenerImpl {

    protected MixinClientPacketListener(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "handleSetSpawn", at = @At("RETURN"))
    private void moveDownloadingTerrainClosing(ClientboundSetDefaultSpawnPositionPacket packet, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(ProtocolVersion.v1_18_2, ProtocolVersion.v1_20_2) && this.minecraft.screen instanceof ILevelLoadingScreen mixinDownloadingTerrainScreen) {
            mixinDownloadingTerrainScreen.viaFabricPlus$setReady();
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void closeDownloadingTerrain(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18) && this.minecraft.screen instanceof ILevelLoadingScreen mixinDownloadingTerrainScreen) {
            mixinDownloadingTerrainScreen.viaFabricPlus$setReady();
        }
    }

}

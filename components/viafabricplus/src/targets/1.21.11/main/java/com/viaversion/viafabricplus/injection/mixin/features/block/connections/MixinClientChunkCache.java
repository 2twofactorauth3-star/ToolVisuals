package com.viaversion.viafabricplus.injection.mixin.features.block.connections;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;

import com.viaversion.viafabricplus.features.block.connections.BlockConnectionsEmulation1_12_2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientChunkCache {

    @Shadow
    private ClientLevel level;

    @Inject(method = "updateLevelChunk", at = @At("TAIL"))
    private void updateBlockConnections(int chunkX, int chunkZ, ClientboundLevelChunkPacketData clientboundLevelChunkPacketData, CallbackInfo ci) {
        BlockConnectionsEmulation1_12_2.updateChunkNeighborConnections(this.level, chunkX, chunkZ);
    }

    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    private void updateBlockConnections(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket, CallbackInfo ci) {
        BlockConnectionsEmulation1_12_2.updateChunkNeighborConnections(this.level, clientboundBlockUpdatePacket.getPos());
    }

    @Inject(method = "handleBlockEvent", at = @At("TAIL"))
    private void updateBlockConnections(ClientboundBlockEventPacket clientboundBlockEventPacket, CallbackInfo ci) {
        BlockConnectionsEmulation1_12_2.updateChunkNeighborConnections(this.level, clientboundBlockEventPacket.getPos());
    }

    @Inject(method = "handleBlockDestruction", at = @At("TAIL"))
    private void updateBlockConnections(ClientboundBlockDestructionPacket clientboundBlockDestructionPacket, CallbackInfo ci) {
        BlockConnectionsEmulation1_12_2.updateChunkNeighborConnections(this.level, clientboundBlockDestructionPacket.getPos());
    }

}

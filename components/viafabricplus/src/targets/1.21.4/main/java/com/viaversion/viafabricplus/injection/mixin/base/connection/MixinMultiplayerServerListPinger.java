package com.viaversion.viafabricplus.injection.mixin.base.connection;

import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.injection.access.base.IMultiValueDebugSampleLogImpl;
import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.net.InetSocketAddress;

@Mixin(MultiplayerServerListPinger.class)
public final class MixinMultiplayerServerListPinger {

    @WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/util/profiler/MultiValueDebugSampleLogImpl;)Lnet/minecraft/network/ClientConnection;"))
    private ClientConnection setForcedVersion(InetSocketAddress address, boolean useEpoll, MultiValueDebugSampleLogImpl packetSizeLog, Operation<ClientConnection> original, @Local(argsOnly = true) ServerInfo serverInfo) {
        final IServerInfo mixinServerInfo = (IServerInfo) serverInfo;

        if (mixinServerInfo.viaFabricPlus$forcedVersion() != null && !mixinServerInfo.viaFabricPlus$passedDirectConnectScreen()) {

            if (packetSizeLog == null) {
                packetSizeLog = new MultiValueDebugSampleLogImpl(1);
            }

            ((IMultiValueDebugSampleLogImpl) packetSizeLog).viaFabricPlus$setForcedVersion(mixinServerInfo.viaFabricPlus$forcedVersion());
            mixinServerInfo.viaFabricPlus$passDirectConnectScreen(false);
        }

        return original.call(address, useEpoll, packetSizeLog);
    }

}

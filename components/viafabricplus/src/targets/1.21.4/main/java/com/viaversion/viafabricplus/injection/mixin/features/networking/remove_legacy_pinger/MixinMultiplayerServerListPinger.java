package com.viaversion.viafabricplus.injection.mixin.features.networking.remove_legacy_pinger;

import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.net.InetSocketAddress;

@Mixin(MultiplayerServerListPinger.class)
public abstract class MixinMultiplayerServerListPinger {

    @Overwrite
    public void ping(InetSocketAddress socketAddress, ServerAddress address, ServerInfo serverInfo) {
    }

}

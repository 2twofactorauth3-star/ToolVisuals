package com.viaversion.viafabricplus.injection.mixin.features.networking.remove_legacy_pinger;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.network.EventLoopGroupHolder;

import java.net.InetSocketAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerStatusPinger.class)
public abstract class MixinServerStatusPinger {

    @Overwrite
    public void pingLegacyServer(InetSocketAddress inetSocketAddress, final ServerAddress serverAddress, final ServerData serverData, EventLoopGroupHolder eventLoopGroupHolder) {
    }

}

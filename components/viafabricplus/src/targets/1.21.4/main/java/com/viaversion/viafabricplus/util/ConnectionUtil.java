package com.viaversion.viafabricplus.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public final class ConnectionUtil {

    public static void connect(final String address, final ProtocolVersion version) {
        connect(address, address, version);
    }

    public static void connect(final String name, final String address) {
        connect(name, address, null);
    }

    public static void connect(final String name, final String address, final ProtocolVersion version) {
        final ServerAddress serverAddress = ServerAddress.parse(address);
        final ServerInfo entry = new ServerInfo(name, serverAddress.getAddress(), ServerInfo.ServerType.OTHER);

        if (version != null) {
            ((IServerInfo) entry).viaFabricPlus$forceVersion(version);
        }
        ConnectScreen.connect(MinecraftClient.getInstance().currentScreen, MinecraftClient.getInstance(), serverAddress, entry, false, null);
    }

}

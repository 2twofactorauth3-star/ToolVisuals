package com.viaversion.viafabricplus.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import com.viaversion.viafabricplus.injection.access.base.IServerData;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public final class ConnectionUtil {
   public static void connect(String address, ProtocolVersion version) {
      connect(address, address, version);
   }

   public static void connect(String name, String address) {
      connect(name, address, null);
   }

   public static void connect(String name, String address, ProtocolVersion version) {
      ServerAddress serverAddress = ServerAddress.parseString(address);
      ServerData entry = new ServerData(name, serverAddress.getHost(), Type.OTHER);
      if (version != null) {
         ((IServerData)entry).viaFabricPlus$forceVersion(version);
      }

      ConnectScreen.startConnecting(Minecraft.getInstance().screen, Minecraft.getInstance(), serverAddress, entry, false, null);
   }
}

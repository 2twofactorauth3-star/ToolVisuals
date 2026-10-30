package com.viaversion.viafabricplus.features.limitation.max_chat_length;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import com.viaversion.viafabricplus.injection.access.base.IConnection;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import net.raphimc.vialegacy.protocol.classic.c0_30cpetoc0_28_30.data.ClassicProtocolExtension;
import net.raphimc.vialegacy.protocol.classic.c0_30cpetoc0_28_30.storage.ExtensionProtocolMetadataStorage;

public final class MaxChatLength {
   public static final int MAX_CHAT_LENGTH_LATEST = 256;

   public static int getChatLength() {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.c0_28toc0_30)) {
         ClientPacketListener var0 = Minecraft.getInstance().getConnection();
         ExtensionProtocolMetadataStorage var1 = (ExtensionProtocolMetadataStorage)((IConnection)var0.getConnection())
            .viaFabricPlus$getUserConnection()
            .get(ExtensionProtocolMetadataStorage.class);
         return var1 != null && var1.hasServerExtension(ClassicProtocolExtension.LONGER_MESSAGES, new int[0])
            ? 65534
            : 64 - (Minecraft.getInstance().getUser().getName().length() + 2);
      } else {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
         return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_9_3) ? 100 : 256;
      }
   }
}

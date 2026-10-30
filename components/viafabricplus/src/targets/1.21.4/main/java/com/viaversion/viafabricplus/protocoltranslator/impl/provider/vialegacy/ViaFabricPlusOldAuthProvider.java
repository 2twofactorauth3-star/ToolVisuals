package com.viaversion.viafabricplus.protocoltranslator.impl.provider.vialegacy;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.settings.impl.AuthenticationSettings;
import com.viaversion.viafabricplus.util.ChatUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.vialegacy.protocol.release.r1_2_4_5tor1_3_1_2.provider.OldAuthProvider;

public final class ViaFabricPlusOldAuthProvider extends OldAuthProvider {

    @Override
    public void sendAuthRequest(UserConnection connection, String serverId) {
        if (!AuthenticationSettings.INSTANCE.verifySessionForOnlineModeServers.getValue()) {
            return;
        }

        try {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.getSessionService().joinServer(client.getSession().getUuidOrNull(), client.getSession().getAccessToken(), serverId);
        } catch (Exception e) {
            connection.getChannel().attr(ProtocolTranslator.CLIENT_CONNECTION_ATTRIBUTE_KEY).get().disconnect(ChatUtil.prefixText(Text.translatable("betacraft.viafabricplus.failed_to_verify_session")));
            ViaFabricPlusImpl.INSTANCE.logger().error("Error occurred while calling join server to verify session", e);
        }
    }

}

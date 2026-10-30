package com.viaversion.viafabricplus.protocoltranslator.impl.provider.viaversion;

import net.minecraft.network.Connection;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.CompressionProvider;

public final class ViaFabricPlusCompressionProvider extends CompressionProvider {

    @Override
    public void handlePlayCompression(final UserConnection user, final int threshold) {
        final Connection connection = user.getChannel().attr(ProtocolTranslator.CLIENT_CONNECTION_ATTRIBUTE_KEY).get();
        connection.setupCompression(threshold, true);
    }

}

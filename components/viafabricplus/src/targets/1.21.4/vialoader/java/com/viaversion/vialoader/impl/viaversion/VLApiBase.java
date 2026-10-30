package com.viaversion.vialoader.impl.viaversion;

import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.buffer.ByteBuf;

public class VLApiBase extends ViaAPIBase<UserConnection> {

    @Override
    public ProtocolVersion getPlayerProtocolVersion(final UserConnection player) {
        return player.getProtocolInfo().protocolVersion();
    }

    @Override
    public void sendRawPacket(final UserConnection player, final ByteBuf packet) {
        player.scheduleSendRawPacket(packet);
    }

}

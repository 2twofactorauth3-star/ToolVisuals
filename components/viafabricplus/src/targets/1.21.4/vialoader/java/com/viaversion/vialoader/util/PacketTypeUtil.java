package com.viaversion.vialoader.util;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypeMap;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.util.List;

public class PacketTypeUtil {

    public static ServerboundPacketType getServerboundPacketType(final String packetName, final UserConnection connection) {
        return getServerboundPacketType(State.PLAY, packetName, connection);
    }

    public static ServerboundPacketType getServerboundPacketType(final State state, final String packetName, final UserConnection connection) {
        final ProtocolInfo protocolInfo = connection.getProtocolInfo();
        return getServerboundPacketType(state, packetName, protocolInfo.protocolVersion(), protocolInfo.serverProtocolVersion());
    }

    public static ServerboundPacketType getServerboundPacketType(final State state, final String packetName, final ProtocolVersion clientVersion, final ProtocolVersion serverVersion) {
        final List<ProtocolPathEntry> protocols = Via.getManager().getProtocolManager().getProtocolPath(clientVersion, serverVersion);
        if (protocols != null) {
            final ProtocolPathEntry last = protocols.get(protocols.size() - 1);
            final PacketTypeMap<? extends ServerboundPacketType> map = last.protocol().getPacketTypesProvider().mappedServerboundPacketTypes().get(state);
            if (map != null) {
                return map.typeByName(packetName);
            }
        }
        return null;
    }

    public static ClientboundPacketType getClientboundPacketType(final String packetName, final UserConnection connection) {
        return getClientboundPacketType(State.PLAY, packetName, connection);
    }

    public static ClientboundPacketType getClientboundPacketType(final State state, final String packetName, final UserConnection connection) {
        final ProtocolInfo protocolInfo = connection.getProtocolInfo();
        return getClientboundPacketType(state, packetName, protocolInfo.protocolVersion(), protocolInfo.serverProtocolVersion());
    }

    public static ClientboundPacketType getClientboundPacketType(final State state, final String packetName, final ProtocolVersion clientVersion, final ProtocolVersion serverVersion) {
        final List<ProtocolPathEntry> protocols = Via.getManager().getProtocolManager().getProtocolPath(clientVersion, serverVersion);
        if (protocols != null) {
            final ProtocolPathEntry last = protocols.get(protocols.size() - 1);
            final PacketTypeMap<? extends ClientboundPacketType> map = last.protocol().getPacketTypesProvider().mappedClientboundPacketTypes().get(state);
            if (map != null) {
                return map.typeByName(packetName);
            }
        }
        return null;
    }

}

package com.viaversion.viafabricplus.protocoltranslator.util;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.util.Formatting;

import com.google.gson.JsonObject;
import com.viaversion.vialoader.util.ProtocolVersionList;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static com.viaversion.viafabricplus.save.AbstractSave.GSON;

public final class ProtocolVersionDetector {

    private static final int TIMEOUT = 3_000;

    public static ProtocolVersion get(final ServerAddress serverAddress, final InetSocketAddress socketAddress, final ProtocolVersion clientVersion) throws Exception {
        try (
            final Socket socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());

            final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final DataOutputStream handshakePacket = new DataOutputStream(byteArrayOutputStream)
        ) {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(TIMEOUT);

            handshakePacket.writeByte(0);

            writeVarInt(handshakePacket, clientVersion.getOriginalVersion());
            if (clientVersion.olderThanOrEqualTo(ProtocolVersion.v1_17)) {
                writeVarString(handshakePacket, serverAddress.getAddress());
                handshakePacket.writeShort(serverAddress.getPort());
            } else {
                writeVarString(handshakePacket, socketAddress.getHostString());
                handshakePacket.writeShort(socketAddress.getPort());
            }
            writeVarInt(handshakePacket, ConnectionIntent.STATUS.getId());

            writeVarInt(dataOutputStream, byteArrayOutputStream.size());
            dataOutputStream.write(byteArrayOutputStream.toByteArray());

            dataOutputStream.writeByte(1);
            dataOutputStream.writeByte(0);

            final int size = readVarInt(dataInputStream);
            if (size <= 0) {
                throw new IllegalStateException("Invalid packet size");
            }
            final int id = readVarInt(dataInputStream);
            if (id != 0) {
                throw new IllegalStateException("Invalid packet ID");
            }

            final String response = readVarString(dataInputStream);
            final JsonObject object = GSON.fromJson(response, JsonObject.class);
            if (!object.has("version")) {
                throw new IllegalStateException("Invalid ping response");
            }

            final JsonObject version = object.getAsJsonObject("version");
            if (!version.has("name") || !version.has("protocol")) {
                throw new IllegalStateException("Invalid ping response");
            }

            final int serverVersion = version.get("protocol").getAsInt();

            if (clientVersion.getOriginalVersion() == serverVersion) {
                return clientVersion;
            }

            if (ProtocolVersion.isRegistered(serverVersion)) {
                return ProtocolVersion.getProtocol(serverVersion);
            }

            final String name = version.get("name").getAsString();
            for (final ProtocolVersion protocol : ProtocolVersionList.getProtocolsNewToOld()) {
                for (final String includedVersion : protocol.getIncludedVersions()) {
                    if (name.contains(includedVersion)) {
                        return protocol;
                    }
                }
            }

            throw new RuntimeException("Unable to detect the server version\nServer sent an invalid protocol id: " + serverAddress + " (" + name + Formatting.RESET + ")");
        }
    }

    private static int readVarInt(final DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        byte b;
        do {
            b = in.readByte();
            i |= (b & 127) << j++ * 7;
            if (j > 5) {
                throw new IOException("Var int too big");
            }
        } while ((b & 128) == 128);

        return i;
    }

    private static String readVarString(final DataInputStream in) throws IOException {
        final int length = readVarInt(in);
        if (length > Short.MAX_VALUE * 4) {
            throw new IOException("Cannot receive string longer than Short.MAX_VALUE * 4 bytes (got " + length + " bytes)");
        }
        if (length < 0) {
            throw new IOException("Cannot receive string shorter than 0 bytes (got " + length + " bytes)");
        }

        final byte[] bytes = new byte[length];
        in.readFully(bytes);
        final String string = new String(bytes, StandardCharsets.UTF_8);
        if (string.length() > Short.MAX_VALUE) {
            throw new IOException("Cannot receive string longer than Short.MAX_VALUE characters (got " + string.length() + " bytes)");
        }

        return string;
    }

    private static void writeVarInt(final DataOutputStream out, int value) throws IOException {
        while ((value & -128) != 0) {
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    private static void writeVarString(final DataOutputStream out, final String value) throws IOException {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

}

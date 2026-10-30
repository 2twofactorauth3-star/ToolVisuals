package com.viaversion.viafabricplus.features.classic.cpe_extension;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.buffer.ByteBuf;
import net.lenni0451.reflect.Enums;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import net.raphimc.vialegacy.protocol.classic.c0_30cpetoc0_28_30.data.ClassicProtocolExtension;
import net.raphimc.vialegacy.protocol.classic.c0_30cpetoc0_28_30.packet.ClientboundPacketsc0_30cpe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class CPEAdditions {

    public final static List<ClassicProtocolExtension> ALLOWED_EXTENSIONS = new ArrayList<>();
    public final static Map<Integer, ClientboundPacketsc0_30cpe> CUSTOM_PACKETS = new HashMap<>();

    public static ClientboundPacketsc0_30cpe EXT_WEATHER_TYPE;

    private static boolean snowing = false;

    public static void init() {
        allowExtension(ClassicProtocolExtension.ENV_WEATHER_TYPE);
        EXT_WEATHER_TYPE = createNewPacket(ClassicProtocolExtension.ENV_WEATHER_TYPE, 31, (user, buf) -> buf.readByte());
    }

    public static boolean isSnowing() {
        return ProtocolTranslator.getTargetVersion().equals(LegacyProtocolVersion.c0_30cpe) && snowing;
    }

    public static void setSnowing(boolean snowing) {
        CPEAdditions.snowing = snowing;
    }

    public static void allowExtension(final ClassicProtocolExtension classicProtocolExtension) {
        ALLOWED_EXTENSIONS.add(classicProtocolExtension);
    }

    public static ClientboundPacketsc0_30cpe createNewPacket(final ClassicProtocolExtension classicProtocolExtension, final int packetId, final BiConsumer<UserConnection, ByteBuf> packetSplitter) {
        final ClientboundPacketsc0_30cpe packet = Enums.newInstance(ClientboundPacketsc0_30cpe.class, classicProtocolExtension.getName(), ClassicProtocolExtension.values().length, new Class[]{int.class, BiConsumer.class}, new Object[]{packetId, packetSplitter});
        Enums.addEnumInstance(ClientboundPacketsc0_30cpe.class, packet);
        CUSTOM_PACKETS.put(packetId, packet);

        return packet;
    }

}

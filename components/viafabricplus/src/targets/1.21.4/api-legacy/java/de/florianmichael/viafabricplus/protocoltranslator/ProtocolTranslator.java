package de.florianmichael.viafabricplus.protocoltranslator;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viafabricplus.LegacyCompatBridge;
import io.netty.channel.Channel;

@Deprecated
public class ProtocolTranslator {

    @Deprecated
    public static ProtocolVersion getTargetVersion() {
        LegacyCompatBridge.warn();
        return ViaFabricPlus.getImpl().getTargetVersion();
    }

    @Deprecated
    public static ProtocolVersion getTargetVersion(final Channel channel) {
        LegacyCompatBridge.warn();
        return ViaFabricPlus.getImpl().getTargetVersion(channel);
    }

    @Deprecated
    public static void setTargetVersion(final ProtocolVersion newVersion) {
        LegacyCompatBridge.warn();
        ViaFabricPlus.getImpl().setTargetVersion(newVersion);
    }

    @Deprecated
    public static void setTargetVersion(final ProtocolVersion newVersion, final boolean revertOnDisconnect) {
        LegacyCompatBridge.warn();
        ViaFabricPlus.getImpl().setTargetVersion(newVersion, revertOnDisconnect);
    }

    @Deprecated
    public static UserConnection createDummyUserConnection(final ProtocolVersion clientVersion, final ProtocolVersion serverVersion) {
        LegacyCompatBridge.warn();
        return null;
    }

    @Deprecated
    public static UserConnection getPlayNetworkUserConnection() {
        LegacyCompatBridge.warn();
        return ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
    }

}

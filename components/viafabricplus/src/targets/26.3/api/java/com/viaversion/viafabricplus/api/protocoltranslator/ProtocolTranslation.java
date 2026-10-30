package com.viaversion.viafabricplus.api.protocoltranslator;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import io.netty.channel.Channel;
import java.util.Comparator;
import java.util.function.BiConsumer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;

public interface ProtocolTranslation {

    ProtocolVersion AUTO_DETECT_VERSION = new ProtocolVersion(VersionType.SPECIAL, -2, -1, "Auto Detect (1.7+ servers)", null) {
        @Override
        protected Comparator<ProtocolVersion> customComparator() {
            return (o1, o2) -> {
                if (o1 == AUTO_DETECT_VERSION) {
                    return 1;
                } else if (o2 == AUTO_DETECT_VERSION) {
                    return -1;
                } else {
                    return 0;
                }
            };
        }

        @Override
        public boolean isKnown() {
            return false;
        }
    };

    ProtocolVersion targetVersion();

    ProtocolVersion targetVersion(final Connection connection);

    ProtocolVersion targetVersion(final Channel channel);

    default void setTargetVersion(final ProtocolVersion targetVersion) {
        this.setTargetVersion(targetVersion, false);
    }

    void setTargetVersion(final ProtocolVersion targetVersion, final boolean revertOnDisconnect);

    void addChangeProtocolVersionListener(final BiConsumer<ProtocolVersion, ProtocolVersion> listener);

    @Nullable UserConnection userConnection();

    @Nullable UserConnection userConnection(final Connection connection);

    @Nullable ProtocolVersion serverVersion(final ServerData serverData);

    void setServerVersion(final ServerData serverData, final ProtocolVersion version);

}

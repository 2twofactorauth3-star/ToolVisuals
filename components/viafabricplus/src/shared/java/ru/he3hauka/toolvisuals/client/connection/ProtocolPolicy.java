package ru.he3hauka.toolvisuals.client.connection;

import java.util.List;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;

public final class ProtocolPolicy {
    public static final ProtocolVersion MINIMUM = java.util.Objects.requireNonNull(ProtocolVersion.getClosest("1.7.2"));

    private ProtocolPolicy() {}

    public static boolean supported(ProtocolVersion version, ProtocolVersion maximum) {
        return version != null && version != ProtocolVersion.unknown && !version.isSnapshot()
                && version.getVersionType() == VersionType.RELEASE
                && version.newerThanOrEqualTo(MINIMUM) && version.olderThanOrEqualTo(maximum);
    }

    public static List<ProtocolVersion> catalog(List<ProtocolVersion> versions, ProtocolVersion maximum) {
        return versions.stream().filter(version -> supported(version, maximum)).distinct().toList();
    }

    public static ProtocolVersion normalize(ProtocolVersion version, ProtocolVersion nativeVersion) {
        return supported(version, nativeVersion) ? version : nativeVersion;
    }

    public static ProtocolVersion normalizeNullable(ProtocolVersion version, ProtocolVersion nativeVersion) {
        return version == null ? null : normalize(version, nativeVersion);
    }

    public static ProtocolVersion byName(String name, ProtocolVersion nativeVersion) {
        return normalize(name == null ? null : ProtocolVersion.getClosest(name), nativeVersion);
    }
}

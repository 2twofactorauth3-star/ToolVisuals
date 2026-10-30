package com.viaversion.viafabricplus.injection.access.base;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public interface IServerInfo {

    ProtocolVersion viaFabricPlus$forcedVersion();

    void viaFabricPlus$forceVersion(final ProtocolVersion version);

    boolean viaFabricPlus$passedDirectConnectScreen();

    void viaFabricPlus$passDirectConnectScreen(final boolean state);

    ProtocolVersion viaFabricPlus$translatingVersion();

    void viaFabricPlus$setTranslatingVersion(final ProtocolVersion version);

}

package com.viaversion.viafabricplus.injection.access.base;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public interface ILocalSampleLogger {

    ProtocolVersion viaFabricPlus$getForcedVersion();

    void viaFabricPlus$setForcedVersion(final ProtocolVersion version);

}

package com.viaversion.viafabricplus.api.events;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public interface ChangeProtocolVersionCallback {

    void onChangeProtocolVersion(final ProtocolVersion oldVersion, final ProtocolVersion newVersion);

}

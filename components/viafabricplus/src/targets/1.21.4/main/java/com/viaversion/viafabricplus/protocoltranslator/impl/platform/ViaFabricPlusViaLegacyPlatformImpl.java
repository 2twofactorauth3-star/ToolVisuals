package com.viaversion.viafabricplus.protocoltranslator.impl.platform;

import com.viaversion.vialoader.impl.platform.ViaLegacyPlatformImpl;
import com.viaversion.viaversion.api.Via;

public final class ViaFabricPlusViaLegacyPlatformImpl extends ViaLegacyPlatformImpl {

    @Override
    public String getCpeAppName() {
        return Via.getPlatform().getPlatformName() + " " + Via.getPlatform().getPlatformVersion();
    }

}

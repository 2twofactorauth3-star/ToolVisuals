package com.viaversion.viafabricplus.api.entrypoint;

import com.viaversion.viafabricplus.api.ViaFabricPlusBase;

@FunctionalInterface
public interface ViaFabricPlusLoadEntrypoint {

    void onPlatformLoad(final ViaFabricPlusBase platform);

}

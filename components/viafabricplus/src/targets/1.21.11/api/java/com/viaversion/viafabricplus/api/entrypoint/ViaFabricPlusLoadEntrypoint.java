package com.viaversion.viafabricplus.api.entrypoint;

import com.viaversion.viafabricplus.api.ViaFabricPlusBase;
import com.viaversion.viafabricplus.api.events.LoadingCycleCallback;

@FunctionalInterface
public interface ViaFabricPlusLoadEntrypoint {

    String KEY = "viafabricplus";

    void onPlatformLoad(final ViaFabricPlusBase platform);

}

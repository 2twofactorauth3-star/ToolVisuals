package com.viaversion.viafabricplus;

import com.viaversion.viafabricplus.api.ViaFabricPlusBase;
import org.jetbrains.annotations.ApiStatus;

public final class ViaFabricPlus {

    private static ViaFabricPlusBase impl;

    @ApiStatus.Internal
    public static void init(final ViaFabricPlusBase impl) {
        if (ViaFabricPlus.impl != null) {
            throw new IllegalStateException("ViaFabricPlus has already been initialized!");
        }
        ViaFabricPlus.impl = impl;
    }

    public static ViaFabricPlusBase getImpl() {
        if (impl == null) {
            throw new IllegalStateException("ViaFabricPlus has not been initialized yet!");
        }
        return impl;
    }

}

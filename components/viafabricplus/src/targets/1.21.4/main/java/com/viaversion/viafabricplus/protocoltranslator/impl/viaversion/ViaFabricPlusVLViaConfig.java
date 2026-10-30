package com.viaversion.viafabricplus.protocoltranslator.impl.viaversion;

import com.viaversion.vialoader.impl.viaversion.VLViaConfig;

import java.io.File;
import java.util.logging.Logger;

public final class ViaFabricPlusVLViaConfig extends VLViaConfig {

    public ViaFabricPlusVLViaConfig(File configFile, Logger logger) {
        super(configFile, logger);

        UNSUPPORTED.add("simulate-pt");
        UNSUPPORTED.add("fix-1_21-placement-rotation");
    }

    @Override
    public boolean isSimulatePlayerTick() {
        return false;
    }

    @Override
    public boolean fix1_21PlacementRotation() {
        return false;
    }

}

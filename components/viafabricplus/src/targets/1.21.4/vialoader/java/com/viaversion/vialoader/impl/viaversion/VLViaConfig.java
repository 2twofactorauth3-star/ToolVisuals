package com.viaversion.vialoader.impl.viaversion;

import com.viaversion.viaversion.configuration.AbstractViaConfig;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class VLViaConfig extends AbstractViaConfig {

    protected final List<String> UNSUPPORTED = new ArrayList<>();

    public VLViaConfig(final File configFile, final Logger logger) {
        super(configFile, logger);

        UNSUPPORTED.addAll(BUKKIT_ONLY_OPTIONS);
        UNSUPPORTED.addAll(VELOCITY_ONLY_OPTIONS);
        UNSUPPORTED.add("check-for-updates");
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return Collections.unmodifiableList(UNSUPPORTED);
    }

    @Override
    public boolean isCheckForUpdates() {
        return false;
    }

}

package com.viaversion.vialoader.impl.platform;

import com.viaversion.viaaprilfools.platform.ViaAprilFoolsPlatform;
import com.viaversion.vialoader.util.JLoggerToSLF4J;
import com.viaversion.viaversion.api.Via;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.logging.Logger;

public class ViaAprilFoolsPlatformImpl implements ViaAprilFoolsPlatform {

    private static final Logger LOGGER = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaAprilFools"));

    public ViaAprilFoolsPlatformImpl() {
        this.init(new File(this.getDataFolder(), "viaaprilfools.yml"));
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public File getDataFolder() {
        return Via.getPlatform().getDataFolder();
    }

}

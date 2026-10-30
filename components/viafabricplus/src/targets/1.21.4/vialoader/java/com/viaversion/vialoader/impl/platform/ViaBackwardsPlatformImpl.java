package com.viaversion.vialoader.impl.platform;

import com.viaversion.viabackwards.api.ViaBackwardsPlatform;
import com.viaversion.viaversion.api.Via;
import com.viaversion.vialoader.util.JLoggerToSLF4J;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.logging.Logger;

public class ViaBackwardsPlatformImpl implements ViaBackwardsPlatform {

    private static final Logger LOGGER = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaBackwards"));

    public ViaBackwardsPlatformImpl() {
        this.init(new File(this.getDataFolder(), "viabackwards.yml"));
        this.enable();
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public File getDataFolder() {
        return Via.getPlatform().getDataFolder();
    }

    @Override
    public boolean isOutdated() {
        return false;
    }

    @Override
    public void disable() {
    }

}

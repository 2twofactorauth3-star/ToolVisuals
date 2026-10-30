package de.florianmichael.viafabricplus;

import de.florianmichael.viafabricplus.save.SaveManager;
import de.florianmichael.viafabricplus.settings.SettingsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Deprecated
public class ViaFabricPlus {

    static final ViaFabricPlus INSTANCE = new ViaFabricPlus();

    private final Logger logger = LogManager.getLogger("ViaFabricPlus-Legacy");

    private final SettingsManager settingsManager = new SettingsManager();
    private final SaveManager saveManager = new SaveManager();

    @Deprecated
    public static ViaFabricPlus global() {
        LegacyCompatBridge.warn();
        return INSTANCE;
    }

    @Deprecated
    public Logger getLogger() {
        return logger;
    }

    @Deprecated
    public File getDirectory() {
        return com.viaversion.viafabricplus.ViaFabricPlus.getImpl().rootPath().toFile();
    }

    @Deprecated
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    @Deprecated
    public SaveManager getSaveManager() {
        return saveManager;
    }

}

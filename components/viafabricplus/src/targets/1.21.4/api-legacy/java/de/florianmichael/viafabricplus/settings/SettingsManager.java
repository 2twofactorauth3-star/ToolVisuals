package de.florianmichael.viafabricplus.settings;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.settings.SettingGroup;

import java.util.List;

@Deprecated
public class SettingsManager {

    @Deprecated
    public void addGroup(final SettingGroup... groups) {
        for (SettingGroup group : groups) {
            ViaFabricPlus.getImpl().addSettingGroup(group);
        }
    }

    @Deprecated
    public List<SettingGroup> getGroups() {
        return ViaFabricPlus.getImpl().settingGroups();
    }

}

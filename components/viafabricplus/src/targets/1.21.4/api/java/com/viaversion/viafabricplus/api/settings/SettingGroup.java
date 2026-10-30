package com.viaversion.viafabricplus.api.settings;

import net.minecraft.text.Text;

import com.viaversion.viafabricplus.util.ChatUtil;
import java.util.ArrayList;
import java.util.List;

public class SettingGroup {

    private final List<AbstractSetting<?>> settings = new ArrayList<>();
    private final Text name;

    public SettingGroup(Text name) {
        this.name = name;
    }

    public List<AbstractSetting<?>> getSettings() {
        return settings;
    }

    public AbstractSetting<?> getSetting(final String translationKey) {
        for (AbstractSetting<?> setting : settings) {
            if (ChatUtil.uncoverTranslationKey(setting.getName()).equals(translationKey)) {
                return setting;
            }
        }
        return null;
    }

    public Text getName() {
        return name;
    }

}

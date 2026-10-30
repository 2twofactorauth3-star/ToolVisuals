package com.viaversion.viafabricplus.api.settings;

import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.util.ChatUtil;
import java.util.ArrayList;
import java.util.List;

public class SettingGroup {

    private final List<AbstractSetting<?>> settings = new ArrayList<>();
    private final Component name;

    public SettingGroup(Component name) {
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

    public Component getName() {
        return name;
    }

}

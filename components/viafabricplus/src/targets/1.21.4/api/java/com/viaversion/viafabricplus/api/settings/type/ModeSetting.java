package com.viaversion.viafabricplus.api.settings.type;

import net.minecraft.text.MutableText;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.api.settings.AbstractSetting;
import com.viaversion.viafabricplus.api.settings.SettingGroup;
import com.viaversion.viafabricplus.util.ChatUtil;
import java.util.Arrays;

public class ModeSetting extends AbstractSetting<MutableText> {

    private final MutableText[] options;

    public ModeSetting(SettingGroup parent, MutableText name, MutableText... options) {
        this(parent, name, 0, options);
    }

    public ModeSetting(SettingGroup parent, MutableText name, int defaultOption, MutableText... options) {
        super(parent, name, options[defaultOption]);
        this.options = options;
    }

    @Override
    public void write(JsonObject object) {
        object.addProperty(getTranslationKey(), mapTranslationKey(ChatUtil.uncoverTranslationKey(getValue())));
    }

    @Override
    public void read(JsonObject object) {
        final String selected = object.get(getTranslationKey()).getAsString();
        for (MutableText option : options) {
            if (mapTranslationKey(ChatUtil.uncoverTranslationKey(option)).equals(selected)) {
                setValue(option);
                break;
            }
        }
    }

    public void setValue(int index) {
        super.setValue(options[index]);
    }

    public int getIndex() {
        return Arrays.stream(options).toList().indexOf(getValue());
    }

    public MutableText[] getOptions() {
        return options;
    }

}

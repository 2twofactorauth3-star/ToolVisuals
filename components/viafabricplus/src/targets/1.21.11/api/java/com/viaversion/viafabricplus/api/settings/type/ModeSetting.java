package com.viaversion.viafabricplus.api.settings.type;

import net.minecraft.network.chat.MutableComponent;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.api.settings.AbstractSetting;
import com.viaversion.viafabricplus.api.settings.SettingGroup;
import com.viaversion.viafabricplus.util.ChatUtil;
import java.util.Arrays;

public class ModeSetting extends AbstractSetting<MutableComponent> {

    private final MutableComponent[] options;

    public ModeSetting(SettingGroup parent, MutableComponent name, MutableComponent... options) {
        this(parent, name, 0, options);
    }

    public ModeSetting(SettingGroup parent, MutableComponent name, int defaultOption, MutableComponent... options) {
        super(parent, name, options[defaultOption]);
        this.options = options;
    }

    @Override
    public void write(JsonObject object) {
        object.addProperty(getTranslationKey(), mapTranslationKey(ChatUtil.uncoverTranslationKey(getCurrentValue())));
    }

    @Override
    public void read(JsonObject object) {
        final String selected = object.get(getTranslationKey()).getAsString();
        for (MutableComponent option : options) {
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

    public MutableComponent[] getOptions() {
        return options;
    }

}

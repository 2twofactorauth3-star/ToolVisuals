package com.viaversion.viafabricplus.api.settings.type;

import net.minecraft.network.chat.MutableComponent;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.api.settings.AbstractSetting;
import com.viaversion.viafabricplus.api.settings.SettingGroup;

public class BooleanSetting extends AbstractSetting<Boolean> {

    public BooleanSetting(SettingGroup parent, MutableComponent name, Boolean defaultValue) {
        super(parent, name, defaultValue);
    }

    @Override
    public void write(JsonObject object) {
        object.addProperty(getTranslationKey(), getCurrentValue());
    }

    @Override
    public void read(JsonObject object) {
        setValue(object.get(getTranslationKey()).getAsBoolean());
    }

}

package com.viaversion.viafabricplus.api.settings.type;

import net.minecraft.text.MutableText;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.api.settings.AbstractSetting;
import com.viaversion.viafabricplus.api.settings.SettingGroup;

public class BooleanSetting extends AbstractSetting<Boolean> {

    public BooleanSetting(SettingGroup parent, MutableText name, Boolean defaultValue) {
        super(parent, name, defaultValue);
    }

    @Override
    public void write(JsonObject object) {
        object.addProperty(getTranslationKey(), getValue());
    }

    @Override
    public void read(JsonObject object) {
        setValue(object.get(getTranslationKey()).getAsBoolean());
    }

}

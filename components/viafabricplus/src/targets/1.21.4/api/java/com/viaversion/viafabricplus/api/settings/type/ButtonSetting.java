package com.viaversion.viafabricplus.api.settings.type;

import net.minecraft.text.MutableText;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.api.settings.AbstractSetting;
import com.viaversion.viafabricplus.api.settings.SettingGroup;

public class ButtonSetting extends AbstractSetting<Runnable> {

    public ButtonSetting(SettingGroup parent, MutableText name, Runnable onClick) {
        super(parent, name, onClick);
    }

    public MutableText displayValue() {
        return getName();
    }

    @Override
    public void write(JsonObject object) {
    }

    @Override
    public void read(JsonObject object) {
    }

}

package com.viaversion.viafabricplus.api.settings;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.util.ChatUtil;
import java.util.function.Supplier;

public abstract class AbstractSetting<T> {

    private final MutableText name;
    private final T defaultValue;

    private T value;

    private Supplier<Text> tooltip;

    public AbstractSetting(final SettingGroup parent, final MutableText name, final T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;

        this.value = defaultValue;

        parent.getSettings().add(this);
    }

    public abstract void write(final JsonObject object);

    public abstract void read(final JsonObject object);

    public void onValueChanged() {
    }

    public MutableText getName() {
        return name;
    }

    public String getTranslationKey() {
        return mapTranslationKey(ChatUtil.uncoverTranslationKey(name));
    }

    public static String mapTranslationKey(final String input) {
        return input.split("viafabricplus.")[1];
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        onValueChanged();
    }

    public Text getTooltip() {
        if (tooltip == null) {
            return null;
        } else {
            return tooltip.get();
        }
    }

    public void setTooltip(Text tooltip) {
        this.tooltip = () -> tooltip;
    }

    public void setTooltip(Supplier<Text> tooltip) {
        this.tooltip = tooltip;
    }

}

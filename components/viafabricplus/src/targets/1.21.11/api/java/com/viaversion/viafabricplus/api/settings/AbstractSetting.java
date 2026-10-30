package com.viaversion.viafabricplus.api.settings;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.util.ChatUtil;
import java.util.function.Supplier;

public abstract class AbstractSetting<T> {

    private final MutableComponent name;
    private final T defaultValue;

    private T value;

    private Supplier<Component> tooltip;

    private boolean locked;
    private T lockedValue;

    public AbstractSetting(final SettingGroup parent, final MutableComponent name, final T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;

        this.value = defaultValue;

        parent.getSettings().add(this);
    }

    public abstract void write(final JsonObject object);

    public abstract void read(final JsonObject object);

    public void onValueChanged() {
    }

    public MutableComponent getName() {
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
        if (!locked) {
            return value;
        }

        if (lockedValue == null) {

            lockedValue = defaultValue;
        }
        return lockedValue;
    }

    public T getCurrentValue() {
        return value;
    }

    public void setValue(T value) {
        if (locked && lockedValue == null) {
            this.lockedValue = value;
        }
        this.value = value;
        onValueChanged();
    }

    public Component getTooltip() {
        if (tooltip == null) {
            return null;
        } else {
            return tooltip.get();
        }
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = () -> tooltip;
    }

    public void setTooltip(Supplier<Component> tooltip) {
        this.tooltip = tooltip;
    }

    public void lockValue() {
        this.locked = true;
        setTooltip(Component.translatable("base.viafabricplus.this_will_require_a_restart"));
    }

}

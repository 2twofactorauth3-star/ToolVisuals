package com.viaversion.viafabricplus.api.settings.type;

import net.minecraft.text.MutableText;

import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.settings.AbstractSetting;
import com.viaversion.viafabricplus.api.settings.SettingGroup;
import com.viaversion.vialoader.util.VersionRange;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public class VersionedBooleanSetting extends AbstractSetting<Integer> {

    public static final int AUTO_INDEX = 2;
    public static final int DISABLED_INDEX = 1;
    public static final int ENABLED_INDEX = 0;

    private final VersionRange protocolRange;

    public VersionedBooleanSetting(SettingGroup parent, MutableText name, VersionRange protocolRange) {
        super(parent, name, AUTO_INDEX);

        this.protocolRange = protocolRange;
    }

    @Override
    public void write(JsonObject object) {
        object.addProperty(getTranslationKey(), getValue() == AUTO_INDEX ? "auto" : getValue() == ENABLED_INDEX ? "enabled" : "disabled");
    }

    @Override
    public void read(JsonObject object) {
        final String selected = object.get(getTranslationKey()).getAsString();

        setValue(selected.equals("auto") ? AUTO_INDEX : selected.equals("enabled") ? ENABLED_INDEX : DISABLED_INDEX);
    }

    public boolean isAuto() {
        return getValue() == AUTO_INDEX;
    }

    public boolean isEnabled() {
        return isEnabled(ViaFabricPlus.getImpl().getTargetVersion());
    }

    public boolean isEnabled(final ProtocolVersion version) {
        if (isAuto()) {
            return protocolRange.contains(version);
        } else {
            return getValue() == ENABLED_INDEX;
        }
    }

    public VersionRange getProtocolRange() {
        return protocolRange;
    }

}

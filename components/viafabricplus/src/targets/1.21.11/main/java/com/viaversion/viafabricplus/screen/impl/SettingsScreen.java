package com.viaversion.viafabricplus.screen.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.api.settings.AbstractSetting;
import com.viaversion.viafabricplus.api.settings.SettingGroup;
import com.viaversion.viafabricplus.api.settings.type.BooleanSetting;
import com.viaversion.viafabricplus.api.settings.type.ButtonSetting;
import com.viaversion.viafabricplus.api.settings.type.ModeSetting;
import com.viaversion.viafabricplus.api.settings.type.VersionedBooleanSetting;
import com.viaversion.viafabricplus.screen.VFPList;
import com.viaversion.viafabricplus.screen.VFPScreen;
import com.viaversion.viafabricplus.screen.impl.settings.BooleanListEntry;
import com.viaversion.viafabricplus.screen.impl.settings.ButtonListEntry;
import com.viaversion.viafabricplus.screen.impl.settings.ModeListEntry;
import com.viaversion.viafabricplus.screen.impl.settings.TitleEntry;
import com.viaversion.viafabricplus.screen.impl.settings.VersionedBooleanListEntry;
import com.viaversion.viafabricplus.settings.SettingsManager;

public final class SettingsScreen extends VFPScreen {

    public static final SettingsScreen INSTANCE = new SettingsScreen();

    public SettingsScreen() {
        super(Component.translatable("screen.viafabricplus.settings"), true);
    }

    @Override
    protected void init() {
        this.setupDefaultSubtitle();
        this.addRenderableWidget(new SlotList(this.minecraft, width, height, 3 + 3   + (font.lineHeight + 2) * 3  , -5, (font.lineHeight + 2) * 2));

        super.init();
    }

    public static class SlotList extends VFPList {
        private static double scrollAmount;

        public SlotList(Minecraft minecraftClient, int width, int height, int top, int bottom, int entryHeight) {
            super(minecraftClient, width, height, top, bottom, entryHeight);

            for (SettingGroup group : SettingsManager.INSTANCE.getGroups()) {
                this.addEntry(new TitleEntry(group.getName()));

                for (AbstractSetting<?> setting : group.getSettings()) {
                    switch (setting) {
                        case final BooleanSetting booleanSetting -> this.addEntry(new BooleanListEntry(booleanSetting));
                        case final ButtonSetting buttonSetting -> this.addEntry(new ButtonListEntry(buttonSetting));
                        case final ModeSetting modeSetting -> this.addEntry(new ModeListEntry(modeSetting));
                        case final VersionedBooleanSetting versionedBooleanSetting ->
                            this.addEntry(new VersionedBooleanListEntry(versionedBooleanSetting));
                        default ->
                            ViaFabricPlusImpl.INSTANCE.getLogger().warn("Unknown setting type: {}", setting.getClass().getName());
                    }
                }
            }
            initScrollY(scrollAmount);
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 140;
        }

        @Override
        protected void updateSlotAmount(double amount) {
            scrollAmount = amount;
        }
    }

}

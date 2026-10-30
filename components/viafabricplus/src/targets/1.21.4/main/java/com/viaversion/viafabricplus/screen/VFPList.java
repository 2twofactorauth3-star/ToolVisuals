package com.viaversion.viafabricplus.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

import com.viaversion.viafabricplus.screen.impl.PerServerVersionScreen;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;

public class VFPList extends AlwaysSelectedEntryListWidget<VFPListEntry> {

    public VFPList(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int entryHeight) {
        super(minecraftClient, width, height - top - bottom, top, entryHeight);
    }

    public void initScrollY(final double scrollY) {

        if (GeneralSettings.INSTANCE.saveScrollPositionInSlotScreens.getValue()) {
            this.setScrollY(scrollY);
        }
    }

    @Override
    public void setScrollY(double scrollY) {
        super.setScrollY(scrollY);
        updateSlotAmount(getScrollY());
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {

    }

    protected void updateSlotAmount(final double amount) {

    }

}

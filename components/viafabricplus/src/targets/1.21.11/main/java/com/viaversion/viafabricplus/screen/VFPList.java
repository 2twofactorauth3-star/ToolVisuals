package com.viaversion.viafabricplus.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;

import com.viaversion.viafabricplus.screen.impl.PerServerVersionScreen;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;

public class VFPList extends ObjectSelectionList<VFPListEntry> {

    public VFPList(Minecraft minecraftClient, int width, int height, int top, int bottom, int entryHeight) {
        super(minecraftClient, width, height - top - bottom, top, entryHeight);
    }

    public void initScrollY(final double scrollY) {

        if (GeneralSettings.INSTANCE.saveScrollPositionInSlotScreens.getValue()) {
            this.setScrollAmount(scrollY);
        }
    }

    @Override
    public void setScrollAmount(double scrollY) {
        super.setScrollAmount(scrollY);
        updateSlotAmount(scrollAmount());
    }

    @Override
    protected void renderSelection(final GuiGraphics context, final VFPListEntry entry, final int color) {

    }

    protected void updateSlotAmount(final double amount) {

    }

}

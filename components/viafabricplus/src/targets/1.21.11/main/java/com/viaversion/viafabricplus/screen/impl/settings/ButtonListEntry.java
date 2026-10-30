package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.api.settings.type.ButtonSetting;
import com.viaversion.viafabricplus.screen.VFPListEntry;

public final class ButtonListEntry extends VFPListEntry {

    private final ButtonSetting value;

    public ButtonListEntry(ButtonSetting value) {
        this.value = value;
    }

    @Override
    public Component getNarration() {
        return this.value.displayValue();
    }

    @Override
    public void mappedMouseClicked(double mouseX, double mouseY, int button) {
        this.value.getValue().run();
    }

    @Override
    public void mappedRender(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final Font textRenderer = Minecraft.getInstance().font;

        context.drawCenteredString(textRenderer, this.value.displayValue(), entryWidth / 2, entryHeight / 2 - textRenderer.lineHeight / 2, -1);

        renderTooltip(value.getTooltip(), mouseX, mouseY);
    }

}

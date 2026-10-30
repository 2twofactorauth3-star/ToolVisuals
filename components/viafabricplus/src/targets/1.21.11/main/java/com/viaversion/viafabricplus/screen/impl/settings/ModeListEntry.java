package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.api.settings.type.ModeSetting;
import com.viaversion.viafabricplus.screen.VFPListEntry;
import java.util.Arrays;

public final class ModeListEntry extends VFPListEntry {
    private final ModeSetting value;

    public ModeListEntry(ModeSetting value) {
        this.value = value;
    }

    @Override
    public Component getNarration() {
        return this.value.getName();
    }

    @Override
    public void mappedMouseClicked(double mouseX, double mouseY, int button) {
        final int currentIndex = Arrays.stream(this.value.getOptions()).toList().indexOf(this.value.getCurrentValue()) + 1;
        this.value.setValue(currentIndex > this.value.getOptions().length - 1 ? 0 : currentIndex);
    }

    @Override
    public void mappedRender(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final Font textRenderer = Minecraft.getInstance().font;

        final int offset = textRenderer.width(this.value.getCurrentValue()) + 2;
        renderScrollableText(this.value.getName().withStyle(ChatFormatting.GRAY), offset);
        context.drawString(textRenderer, this.value.getCurrentValue(), entryWidth - offset, entryHeight / 2 - textRenderer.lineHeight / 2, -1);

        renderTooltip(value.getTooltip(), mouseX, mouseY);
    }

}

package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.api.settings.type.BooleanSetting;
import com.viaversion.viafabricplus.screen.VFPListEntry;
import java.awt.*;

public final class BooleanListEntry extends VFPListEntry {
    private final BooleanSetting value;

    public BooleanListEntry(BooleanSetting value) {
        this.value = value;
    }

    @Override
    public Component getNarration() {
        return this.value.getName();
    }

    @Override
    public void mappedMouseClicked(double mouseX, double mouseY, int button) {
        this.value.setValue(!this.value.getCurrentValue());
    }

    @Override
    public void mappedRender(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final Font textRenderer = Minecraft.getInstance().font;

        final Component text = this.value.getCurrentValue() ? Component.translatable("base.viafabricplus.on") : Component.translatable("base.viafabricplus.off");

        final int offset = textRenderer.width(text) + 2;
        renderScrollableText(this.value.getName().withStyle(ChatFormatting.GRAY), offset);
        context.drawString(textRenderer, text, entryWidth - offset, entryHeight / 2 - textRenderer.lineHeight / 2, this.value.getCurrentValue() ? Color.GREEN.getRGB() : Color.RED.getRGB());

        renderTooltip(value.getTooltip(), mouseX, mouseY);
    }

}

package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.api.settings.type.VersionedBooleanSetting;
import com.viaversion.viafabricplus.screen.VFPListEntry;
import java.awt.*;

public final class VersionedBooleanListEntry extends VFPListEntry {
    private final VersionedBooleanSetting value;

    public VersionedBooleanListEntry(VersionedBooleanSetting value) {
        this.value = value;
    }

    @Override
    public Component getNarration() {
        return this.value.getName();
    }

    @Override
    public void mappedMouseClicked(double mouseX, double mouseY, int button) {
        this.value.setValue(this.value.getCurrentValue() + 1);
        if (this.value.getCurrentValue() % 3 == 0) this.value.setValue(0);
    }

    @Override
    public void mappedRender(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final Font textRenderer = Minecraft.getInstance().font;

        final boolean isAuto = this.value.getCurrentValue() == VersionedBooleanSetting.AUTO_INDEX;
        final boolean isEnabled = this.value.isEnabled(this.value.getCurrentValue());
        final Component text = Component.translatable("base.viafabricplus." + (isAuto ? "auto" : isEnabled ? "on" : "off"));
        Color color = isAuto ? Color.ORANGE : isEnabled ? Color.GREEN : Color.RED;

        final int offset = textRenderer.width(text) + 2;
        renderScrollableText(Component.nullToEmpty(ChatFormatting.GRAY + this.value.getName().getString() + " " + ChatFormatting.RESET + this.value.getProtocolRange().toString()), offset);
        context.drawString(textRenderer, text, entryWidth - offset, entryHeight / 2 - textRenderer.lineHeight / 2, color.getRGB());

        renderTooltip(value.getTooltip(), mouseX, mouseY);
    }

}

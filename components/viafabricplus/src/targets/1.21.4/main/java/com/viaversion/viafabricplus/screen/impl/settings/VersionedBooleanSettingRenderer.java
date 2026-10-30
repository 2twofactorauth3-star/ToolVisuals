package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.viaversion.viafabricplus.api.settings.type.VersionedBooleanSetting;
import com.viaversion.viafabricplus.screen.VFPListEntry;
import java.awt.*;

public final class VersionedBooleanSettingRenderer extends VFPListEntry {
    private final VersionedBooleanSetting value;

    public VersionedBooleanSettingRenderer(VersionedBooleanSetting value) {
        this.value = value;
    }

    @Override
    public Text getNarration() {
        return this.value.getName();
    }

    @Override
    public void mappedMouseClicked(double mouseX, double mouseY, int button) {
        this.value.setValue(this.value.getValue() + 1);
        if (this.value.getValue() % 3 == 0) this.value.setValue(0);
    }

    @Override
    public void mappedRender(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        final Text text = Text.translatable("base.viafabricplus." + (this.value.isAuto() ? "auto" : this.value.isEnabled() ? "on" : "off"));
        Color color = this.value.isAuto() ? Color.ORANGE : this.value.isEnabled() ? Color.GREEN : Color.RED;

        final int offset = textRenderer.getWidth(text) + 6;
        renderScrollableText(Text.of(Formatting.GRAY + this.value.getName().getString() + " " + Formatting.RESET + this.value.getProtocolRange().toString()), offset);
        context.drawTextWithShadow(textRenderer, text, entryWidth - offset, entryHeight / 2 - textRenderer.fontHeight / 2, color.getRGB());

        renderTooltip(value.getTooltip(), mouseX, mouseY);
    }

}

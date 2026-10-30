package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.viaversion.viafabricplus.api.settings.type.BooleanSetting;
import com.viaversion.viafabricplus.screen.VFPListEntry;
import java.awt.*;

public final class BooleanSettingRenderer extends VFPListEntry {
    private final BooleanSetting value;

    public BooleanSettingRenderer(BooleanSetting value) {
        this.value = value;
    }

    @Override
    public Text getNarration() {
        return this.value.getName();
    }

    @Override
    public void mappedMouseClicked(double mouseX, double mouseY, int button) {
        this.value.setValue(!this.value.getValue());
    }

    @Override
    public void mappedRender(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        final Text text = this.value.getValue() ? Text.translatable("base.viafabricplus.on") : Text.translatable("base.viafabricplus.off");

        final int offset = textRenderer.getWidth(text) + 6;
        renderScrollableText(this.value.getName().formatted(Formatting.GRAY), offset);
        context.drawTextWithShadow(textRenderer, text, entryWidth - offset, entryHeight / 2 - textRenderer.fontHeight / 2, this.value.getValue() ? Color.GREEN.getRGB() : Color.RED.getRGB());

        renderTooltip(value.getTooltip(), mouseX, mouseY);
    }

}

package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.viaversion.viafabricplus.api.settings.type.ModeSetting;
import com.viaversion.viafabricplus.screen.VFPListEntry;
import java.util.Arrays;

public final class ModeSettingRenderer extends VFPListEntry {
    private final ModeSetting value;

    public ModeSettingRenderer(ModeSetting value) {
        this.value = value;
    }

    @Override
    public Text getNarration() {
        return this.value.getName();
    }

    @Override
    public void mappedMouseClicked(double mouseX, double mouseY, int button) {
        final int currentIndex = Arrays.stream(this.value.getOptions()).toList().indexOf(this.value.getValue()) + 1;
        this.value.setValue(currentIndex > this.value.getOptions().length - 1 ? 0 : currentIndex);
    }

    @Override
    public void mappedRender(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        final int offset = textRenderer.getWidth(this.value.getValue()) + 6;
        renderScrollableText(this.value.getName().formatted(Formatting.GRAY), offset);
        context.drawTextWithShadow(textRenderer, this.value.getValue(), entryWidth - offset, entryHeight / 2 - textRenderer.fontHeight / 2, -1);

        renderTooltip(value.getTooltip(), mouseX, mouseY);
    }

}

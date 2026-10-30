package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.viaversion.viafabricplus.screen.VFPListEntry;

public final class TitleRenderer extends VFPListEntry {

    private final Text name;

    public TitleRenderer(Text name) {
        this.name = name;
    }

    @Override
    public Text getNarration() {
        return this.name;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(x, y, 0);
        mappedRender(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        matrices.pop();
    }

    @Override
    public void mappedRender(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        context.drawTextWithShadow(textRenderer, this.name.copy().formatted(Formatting.BOLD), 3, entryHeight / 2 - textRenderer.fontHeight / 2, -1);
    }

}

package com.viaversion.viafabricplus.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import org.jetbrains.annotations.Nullable;

public abstract class VFPListEntry extends AlwaysSelectedEntryListWidget.Entry<VFPListEntry> {

    protected static final int SCISSORS_OFFSET = 4;
    public static final int SLOT_MARGIN = 3;

    private DrawContext context;
    private int x;
    private int y;
    private int entryWidth;
    private int entryHeight;

    public void mappedRender(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

    }

    public void mappedMouseClicked(double mouseX, double mouseY, int button) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mappedMouseClicked(mouseX, mouseY, button);
        ClickableWidget.playClickSound(MinecraftClient.getInstance().getSoundManager());
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void renderScrollableText(final Text name, final int offset) {
        final TextRenderer font = MinecraftClient.getInstance().textRenderer;

        renderScrollableText(name, entryHeight / 2 - font.fontHeight / 2, offset);
    }

    public void renderScrollableText(final Text text, final int textY, final int offset) {
        final TextRenderer font = MinecraftClient.getInstance().textRenderer;

        final int fontWidth = font.getWidth(text);
        if (fontWidth > (entryWidth - offset)) {
            final double time = (double) Util.getMeasuringTimeMs() / 1000.0;
            final double interpolateEnd = fontWidth - (entryWidth - offset - (SCISSORS_OFFSET + SLOT_MARGIN));

            final double interpolatedValue = Math.sin((Math.PI / 2) * Math.cos(Math.PI * 2 * time / Math.max(interpolateEnd * 0.5, 3.0))) / 2.0 + 0.5;

            context.enableScissor(0, 0, entryWidth - offset - SCISSORS_OFFSET, entryHeight);
            context.drawTextWithShadow(font, text, SLOT_MARGIN - (int) MathHelper.lerp(interpolatedValue, 0.0, interpolateEnd), textY, -1);
            context.disableScissor();
        } else {
            context.drawTextWithShadow(font, text, SLOT_MARGIN, textY, -1);
        }
    }

    public void renderTooltip(final @Nullable Text tooltip, final int mouseX, final int mouseY) {
        if (tooltip != null && mouseX >= x && mouseX <= x + entryWidth && mouseY >= y && mouseY <= y + entryHeight) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, mouseX - x, mouseY - y);
        }
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

        this.context = context;
        this.x = x;
        this.y = y;
        this.entryWidth = entryWidth;
        this.entryHeight = entryHeight;

        final MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(x, y, 0);
        context.fill(0, 0, entryWidth - 4  , entryHeight, Integer.MIN_VALUE);
        mappedRender(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        matrices.pop();
    }

}

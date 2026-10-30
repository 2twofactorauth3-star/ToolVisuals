package com.viaversion.viafabricplus.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public abstract class VFPListEntry extends ObjectSelectionList.Entry<VFPListEntry> {

    protected static final int SCISSORS_OFFSET = 4;
    public static final int SLOT_MARGIN = 3;

    private GuiGraphics context;

    public void mappedRender(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

    }

    public void mappedMouseClicked(double mouseX, double mouseY, int button) {

    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent click, final boolean doubled) {
        mappedMouseClicked(click.x(), click.y(), click.button());
        AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
        return super.mouseClicked(click, doubled);
    }

    public void renderScrollableText(final Component name, final int offset) {
        final Font font = Minecraft.getInstance().font;

        renderScrollableText(name, getContentHeight() / 2 - font.lineHeight / 2, offset);
    }

    public void renderScrollableText(final Component text, final int textY, final int offset) {
        final Font font = Minecraft.getInstance().font;

        final int fontWidth = font.width(text);
        if (fontWidth > (getContentWidth() - offset)) {
            final double time = (double) Util.getMillis() / 1000.0;
            final double interpolateEnd = fontWidth - (getContentWidth() - offset - (SCISSORS_OFFSET + SLOT_MARGIN));

            final double interpolatedValue = Math.sin((Math.PI / 2) * Math.cos(Math.PI * 2 * time / Math.max(interpolateEnd * 0.5, 3.0))) / 2.0 + 0.5;

            context.enableScissor(0, 0, getContentWidth() - offset - SCISSORS_OFFSET, getContentHeight());
            context.drawString(font, text, SLOT_MARGIN - (int) Mth.lerp(interpolatedValue, 0.0, interpolateEnd), textY, -1);
            context.disableScissor();
        } else {
            context.drawString(font, text, SLOT_MARGIN, textY, -1);
        }
    }

    public void renderTooltip(final @Nullable Component tooltip, final int mouseX, final int mouseY) {
        if (tooltip != null && mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight()) {
            context.setTooltipForNextFrame(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void renderContent(final GuiGraphics context, final int mouseX, final int mouseY, final boolean hovered, final float deltaTicks) {
        this.context = context;

        final Matrix3x2fStack matrices = context.pose();

        matrices.pushMatrix();
        matrices.translate(getContentX(), getContentY());
        context.fill(0, 0, getContentWidth(), getContentHeight(), Integer.MIN_VALUE);
        mappedRender(context, getContentX(), getContentY(), getContentWidth(), getContentHeight(), mouseX, mouseY, hovered, deltaTicks);
        matrices.popMatrix();
    }

}

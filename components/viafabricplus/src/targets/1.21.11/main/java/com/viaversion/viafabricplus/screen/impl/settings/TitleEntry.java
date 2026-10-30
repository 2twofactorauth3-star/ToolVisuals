package com.viaversion.viafabricplus.screen.impl.settings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.screen.VFPListEntry;
import org.joml.Matrix3x2fStack;

public final class TitleEntry extends VFPListEntry {

    private final Component name;

    public TitleEntry(Component name) {
        this.name = name;
    }

    @Override
    public Component getNarration() {
        return this.name;
    }

    @Override
    public void renderContent(final GuiGraphics context, final int mouseX, final int mouseY, final boolean hovered, final float deltaTicks) {
        final Matrix3x2fStack matrices = context.pose();

        matrices.pushMatrix();
        matrices.translate(getX(), getY());
        mappedRender(context, getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, hovered, deltaTicks);
        matrices.popMatrix();
    }

    @Override
    public void mappedRender(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        final Font textRenderer = Minecraft.getInstance().font;

        context.drawString(textRenderer, this.name.copy().withStyle(ChatFormatting.BOLD), 3, entryHeight / 2 - textRenderer.lineHeight / 2, -1);
    }

}

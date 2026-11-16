package ru.he3hauka.toolvisuals.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;

public class CenteredEditBox extends EditBox {
    public CenteredEditBox(Font font, int x, int y, int width, int height, Component title) {
        super(font, x, y, width, height, title);
    }
    @Override public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(0, UiFont.centeredTextOffset(getHeight()));
            super.extractWidgetRenderState(graphics, mx, my, partial);
        } finally { graphics.pose().popMatrix(); }
    }
}

package ru.he3hauka.toolvisuals.client.music.ui;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;

final class MediaButton extends VisualsButton {
    private final Supplier<UiPaint.Icon> image;
    private final Supplier<UiPalette> colors;
    private final BooleanSupplier selected;
    MediaButton(int x,int y,int size,Component title,Supplier<UiPaint.Icon> image,Supplier<UiPalette> colors,Runnable action,BooleanSupplier selected) {
        super(x,y,size,size,title,Kind.ICON,null,Component.empty(),action,null);
        this.image=image;this.colors=colors;this.selected=selected;setTooltip(Tooltip.create(title));
    }
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial) {
        isHovered=UiPaint.hovered(g,this,mx,my);
        MusicPaint.control(g,image.get(),getX(),getY(),getWidth(),active,selected.getAsBoolean(),isHoveredOrFocused(),colors.get());
    }
}

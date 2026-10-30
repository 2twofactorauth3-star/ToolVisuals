package ru.he3hauka.toolvisuals.client.menu.vanilla;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public abstract class NativeMenuScreen extends Screen {
    protected final Screen parent;
    protected NativeMenuScreen(Screen parent,Component title){super(title);this.parent=parent;}
    protected int contentWidth(){return Math.min(420,width-32);}
    protected int left(){return (width-contentWidth())/2;}
    protected Button button(Component label,int x,int y,int w,Runnable action){return addRenderableWidget(Button.builder(label,unused->action.run()).bounds(x,y,w,20).build());}
    protected EditBox field(Component hint,int x,int y,int w){var field=addRenderableWidget(new EditBox(minecraft.font,x,y,w,20,hint));field.setHint(hint);return field;}
    protected void back(){button(UiI18n.tr("toolvisuals.ui.back"),(width-Math.min(200,contentWidth()))/2,height-28,Math.min(200,contentWidth()),this::onClose);}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int x,int y,float partial){
        super.extractRenderState(graphics,x,y,partial);
        graphics.textRenderer().accept(TextAlignment.CENTER,width/2,16,getTitle());
        labels(graphics);
    }
    protected void labels(GuiGraphicsExtractor graphics){}
}

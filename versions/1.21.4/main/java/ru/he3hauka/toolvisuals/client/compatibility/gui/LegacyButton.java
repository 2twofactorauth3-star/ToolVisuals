package ru.he3hauka.toolvisuals.client.compatibility.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.compatibility.input.*;

public class LegacyButton extends Button {
    protected LegacyButton(int x,int y,int width,int height,Component label,OnPress action,CreateNarration narration){super(x,y,width,height,label,action,narration);}
    @Override protected void renderWidget(net.minecraft.client.gui.GuiGraphics graphics,int x,int y,float partial){renderContents(graphics,x,y,partial);}
    protected void renderContents(net.minecraft.client.gui.GuiGraphics graphics,int x,int y,float partial){super.renderWidget(graphics,x,y,partial);}
    public boolean mouseClicked(MouseButtonEvent event,boolean twice){return super.mouseClicked(event.x(),event.y(),event.button());}
    public boolean keyPressed(KeyEvent event){return super.keyPressed(event.key(),event.scancode(),event.modifiers());}
    @Override public void onPress(){onPress(new KeyEvent(257,0,0));}
    public void onPress(InputWithModifiers input){super.onPress();}
    @Override public void onClick(double x,double y){onClick(new MouseButtonEvent(x,y,new MouseButtonInfo(0,0)),false);}
    public void onClick(MouseButtonEvent event,boolean twice){super.onClick(event.x(),event.y());}
}

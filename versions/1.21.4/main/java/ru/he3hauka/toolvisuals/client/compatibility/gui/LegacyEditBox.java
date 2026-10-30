package ru.he3hauka.toolvisuals.client.compatibility.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.compatibility.input.*;

public class LegacyEditBox extends EditBox {
    private boolean textShadow=true;
    public LegacyEditBox(Font font,int x,int y,int width,int height,Component label){super(font,x,y,width,height,label);}
    public void setTextShadow(boolean shadow){textShadow=shadow;}
    public boolean textShadow(){return textShadow;}
    protected void handleCursor(net.minecraft.client.gui.GuiGraphics graphics,int x,int y){if(isMouseOver(x,y))VersionGui.cursor(VersionGui.Cursor.TEXT);}
    protected void handleCursor(GuiGraphics graphics){if(isMouseOver(net.minecraft.client.Minecraft.getInstance().mouseHandler.xpos(),net.minecraft.client.Minecraft.getInstance().mouseHandler.ypos()))VersionGui.cursor(VersionGui.Cursor.TEXT);}
    @Override public boolean mouseClicked(double x,double y,int button){return mouseClicked(new MouseButtonEvent(x,y,new MouseButtonInfo(button,0)),false);}
    @Override public boolean keyPressed(int key,int scan,int mods){return keyPressed(new KeyEvent(key,scan,mods));}
    public boolean mouseClicked(MouseButtonEvent event,boolean twice){return super.mouseClicked(event.x(),event.y(),event.button());}
    public boolean keyPressed(KeyEvent event){return super.keyPressed(event.key(),event.scancode(),event.modifiers());}
}

package ru.he3hauka.toolvisuals.client.compatibility.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.compatibility.input.*;

public abstract class LegacyScreen extends Screen {
    protected LegacyScreen(Component title){super(title);}
    @Override public void removed(){VersionGui.cursor(VersionGui.Cursor.DEFAULT);super.removed();}
    public void resize(int width,int height){super.resize(minecraft,width,height);}
    private MouseButtonEvent mouse(double x,double y,int button){return new MouseButtonEvent(x,y,new MouseButtonInfo(button,modifiers()));}
    private static int modifiers(){return (Screen.hasShiftDown()?1:0)|(Screen.hasControlDown()?2:0)|(Screen.hasAltDown()?4:0);}
    @Override public boolean mouseClicked(double x,double y,int button){return mouseClicked(mouse(x,y,button),false);}
    @Override public boolean mouseReleased(double x,double y,int button){return mouseReleased(mouse(x,y,button));}
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){return mouseDragged(mouse(x,y,button),dx,dy);}
    @Override public boolean keyPressed(int key,int scancode,int mods){return keyPressed(new KeyEvent(key,scancode,mods));}
    @Override public boolean keyReleased(int key,int scancode,int mods){return keyReleased(new KeyEvent(key,scancode,mods));}
    public boolean keyReleased(KeyEvent event){return super.keyReleased(event.key(),event.scancode(),event.modifiers());}
    public boolean mouseClicked(MouseButtonEvent event,boolean twice){return super.mouseClicked(event.x(),event.y(),event.button());}
    public boolean mouseReleased(MouseButtonEvent event){return super.mouseReleased(event.x(),event.y(),event.button());}
    public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){return super.mouseDragged(event.x(),event.y(),event.button(),dx,dy);}
    public boolean keyPressed(KeyEvent event){return super.keyPressed(event.key(),event.scancode(),event.modifiers());}
}

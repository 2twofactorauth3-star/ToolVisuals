package ru.he3hauka.toolvisuals.client.hand.ui;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import ru.he3hauka.toolvisuals.client.camera.CameraLeases;
import ru.he3hauka.toolvisuals.client.camera.ClientCamera;
import ru.he3hauka.toolvisuals.client.hand.HandSide;
import ru.he3hauka.toolvisuals.client.hand.render.HandRendering;

public final class HandEditorScreen extends Screen {
    private final Screen parent;
    private final HandEditorOverlay controls;

    public HandEditorScreen(Screen parent,HandSide side) {
        super(HandEditorOverlay.text("editor"));this.parent=parent;controls=new HandEditorOverlay(this,true,side);
    }
    @Override protected void init(){controls.finish();KeyMapping.releaseAll();ClientCamera.acquire(minecraft,CameraLeases.Owner.HAND_EDITOR,CameraType.FIRST_PERSON);}
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int x,int y,float partial) {}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int x,int y,float partial){controls.paint(graphics,x,y,partial);}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){return controls.click(event,twice)||super.mouseClicked(event,twice);}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){return controls.move(event)||super.mouseDragged(event,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent event){return controls.release(event)||super.mouseReleased(event);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){return controls.scroll(x,y,vertical)||super.mouseScrolled(x,y,horizontal,vertical);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public void removed(){controls.finish();HandRendering.clear();ClientCamera.release(minecraft,CameraLeases.Owner.HAND_EDITOR);super.removed();}
}

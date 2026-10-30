package ru.he3hauka.toolvisuals.client.crosshair.ui;

import java.util.*;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.crosshair.CrosshairDesign;
import ru.he3hauka.toolvisuals.client.crosshair.CrosshairPainter;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

public final class CrosshairEditorScreen extends VisualEditorScreen {
    private CrosshairDesign design;
    private CrosshairDesign.Draft drawing;
    private ColorStyle color;
    private final ArrayDeque<CrosshairDesign> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    private boolean erase,mirrorX,mirrorY,painting,strokeInk;
    private int brush=1,lastX,lastY;
    public CrosshairEditorScreen(Screen parent){super(parent,tr("draw_title"));design=ToolVisualsClient.CONFIG.crosshairDesign();color=ToolVisualsClient.CONFIG.colorStyle(VisualSetting.CROSSHAIR_COLOR);}
    @Override protected void init(){
        frame(700,474);
        button(388,72,138,30,tr("brush"),null,()->erase=false,()->!erase);
        button(536,72,144,30,tr("eraser"),null,()->erase=true,()->erase);
        button(388,112,138,30,tr("mirror_x"),null,()->mirrorX=!mirrorX,()->mirrorX);
        button(536,112,144,30,tr("mirror_y"),null,()->mirrorY=!mirrorY,()->mirrorY);
        for(int i=0;i<3;i++){final int size=1+i*2;button(388+i*98,152,92,28,tr("brush_size",size),null,()->brush=size,()->brush==size);}
        button(388,192,138,30,tr("undo"),UiPaint.Icon.BACK,this::undo,null);
        button(536,192,144,30,tr("redo"),UiPaint.Icon.ARROW,this::redo,null);
        button(388,232,138,30,tr("clear"),UiPaint.Icon.RESET,()->replace(CrosshairDesign.empty()),null);
        button(536,232,144,30,tr("cross_template"),null,()->replace(CrosshairDesign.cross()),null);
        button(388,274,292,30,tr("colors"),UiPaint.Icon.RENDER,()->minecraft.gui.setScreen(new ColorEditorScreen(this,tr("crosshair"),color,next->color=next,true)),null);
        button(20,434,348,28,tr("cancel"),null,this::onClose,null);
        button(388,434,292,28,tr("apply_crosshair"),UiPaint.Icon.CHECK,()->{ToolVisualsClient.CONFIG.setCrosshairDesign(design,color);onClose();},null);
    }
    private void push(){undo.addLast(design);if(undo.size()>64)undo.removeFirst();redo.clear();}
    private void replace(CrosshairDesign next){push();design=next;}
    private void undo(){if(undo.isEmpty())return;painting=false;drawing=null;redo.addLast(design);design=undo.removeLast();}
    private void redo(){if(redo.isEmpty())return;painting=false;drawing=null;undo.addLast(design);design=redo.removeLast();}
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        int left=px+20,top=py+72,cell=10;
        UiPaint.rounded(g,left-1,top-1,332,332,5,palette.border);
        double time=ColorStyle.clock();
        for(int y=0;y<CrosshairDesign.SIZE;y++)for(int x=0;x<CrosshairDesign.SIZE;x++) {
            int bg=(x+y)%2==0?palette.field:palette.raised;
            g.fill(left+x*cell,top+y*cell,left+(x+1)*cell,top+(y+1)*cell,bg);
            if(x==CrosshairDesign.CENTER||y==CrosshairDesign.CENTER)g.fill(left+x*cell,top+y*cell,left+(x+1)*cell,top+(y+1)*cell,UiPaint.alpha(palette.accent,.13f));
            if(design.pixel(x,y))g.fill(left+x*cell+1,top+y*cell+1,left+(x+1)*cell,top+(y+1)*cell,0xff000000|color.sample(time,(x+y)/64d));
        }
        UiPaint.centered(g,tr("draw_help").getString(),px+20,py+406,348,22,.7f,palette.muted,1);
        UiPaint.text(g,tr("preview"),px+388,py+314,.8f,palette.muted,1);
        g.fill(px+388,py+338,px+534,py+412,0xff17252c);g.fill(px+534,py+338,px+680,py+412,0xffccd4ce);
        CrosshairPainter.design(g,design,color,px+461,py+375,1.5f,1,1,time);
        CrosshairPainter.design(g,design,color,px+607,py+375,1.5f,1,1,time);
        if(mx>=20&&mx<350&&my>=72&&my<402)g.outline(px+20+(mx-20)/10*10,py+72+(my-72)/10*10,10,10,palette.text);
    }
    private void draw(double x,double y,boolean start){
        int cx=Math.clamp((int)((x-20)/10),0,32),cy=Math.clamp((int)((y-72)/10),0,32);
        if(start){push();drawing=new CrosshairDesign.Draft(design);lastX=cx;lastY=cy;}
        drawing.line(lastX,lastY,cx,cy,brush,strokeInk,mirrorX,mirrorY);design=drawing.snapshot();lastX=cx;lastY=cy;
    }
    @Override public boolean mouseClicked(MouseButtonEvent raw,boolean twice){
        var e=adjusted(raw);double x=e.x()-px,y=e.y()-py;
        if(x>=20&&x<350&&y>=72&&y<402&&(e.button()==InputConstants.MOUSE_BUTTON_LEFT||e.button()==InputConstants.MOUSE_BUTTON_RIGHT)){
            setFocused(null);painting=true;strokeInk=!erase&&e.button()!=InputConstants.MOUSE_BUTTON_RIGHT;draw(x,y,true);return true;
        }
        return super.mouseClicked(raw,twice);
    }
    @Override public boolean mouseDragged(MouseButtonEvent raw,double dx,double dy){if(painting){var e=adjusted(raw);draw(e.x()-px,e.y()-py,false);return true;}return super.mouseDragged(raw,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent e){painting=false;drawing=null;return super.mouseReleased(e);}
    @Override public boolean keyPressed(KeyEvent e){if(e.hasControlDown()&&e.key()==InputConstants.KEY_Z){undo();return true;}if(e.hasControlDown()&&e.key()==InputConstants.KEY_Y){redo();return true;}return super.keyPressed(e);}
}

package ru.he3hauka.toolvisuals.client.hand.ui;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hand.HandProperty;
import ru.he3hauka.toolvisuals.client.hand.HandSide;
import ru.he3hauka.toolvisuals.client.hand.editor.HandInteraction;
import ru.he3hauka.toolvisuals.client.hand.editor.HandProjection;
import ru.he3hauka.toolvisuals.client.hand.render.HandRendering;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class HandEditorOverlay {
    private final Screen owner;
    private final boolean dedicated;
    private final HandInteraction drag=new HandInteraction();
    private final List<VisualsButton> buttons=new ArrayList<>();
    private HandSide selected;
    private UiViewport viewport;
    private int screenWidth=-1,screenHeight=-1,toolbarX,toolbarY,toolbarWidth,dragButton;
    private long frame;

    public HandEditorOverlay(Screen owner,boolean dedicated,HandSide selected) {
        this.owner=owner;this.dedicated=dedicated;this.selected=selected;
    }
    public static Component text(String key,Object... args){return UiI18n.tr("toolvisuals.hand."+key,args);}
    public static Component sideName(HandSide side){return text(side==HandSide.RIGHT?"right":"left");}

    private boolean eligible(){return HandRendering.editable()&&Minecraft.getInstance().isWindowActive()&&Minecraft.getInstance().gui.screen()==owner;}
    private void layout() {
        if(screenWidth==owner.width&&screenHeight==owner.height)return;
        finish();
        screenWidth=owner.width;screenHeight=owner.height;
        viewport=new UiViewport(screenWidth,screenHeight,Minecraft.getInstance().getWindow().getGuiScale());
        buttons.clear();toolbarWidth=Math.min(dedicated?560:440,viewport.width-20);
        toolbarX=(viewport.width-toolbarWidth)/2;
        toolbarY=Math.round(viewport.height-(dedicated?12:30/viewport.scale)-30);
        int count=dedicated?5:4,gap=5,unit=(toolbarWidth-gap*(count-1))/count;
        button(unit,"right",()->selected=HandSide.RIGHT,()->selected==HandSide.RIGHT);
        button(unit,"left",()->selected=HandSide.LEFT,()->selected==HandSide.LEFT);
        button(unit,"reset",()->HandProperty.reset(ToolVisualsClient.CONFIG,selected),null);
        button(unit,"settings",()->{finish();Minecraft.getInstance().gui.setScreen(new HandSettingsScreen(owner,selected));},null);
        if(dedicated)button(unit,"done",owner::onClose,null);
        frame=System.nanoTime();
    }
    private void button(int width,String key,Runnable action,java.util.function.BooleanSupplier state) {
        buttons.add(new VisualsButton(toolbarX+buttons.size()*(width+5),toolbarY,width,28,text(key),VisualsButton.Kind.CHIP,
                null,Component.empty(),action,state));
    }
    private MouseButtonEvent adjusted(MouseButtonEvent event){return new MouseButtonEvent(event.x()/viewport.scale,event.y()/viewport.scale,event.buttonInfo());}
    private boolean toolbar(double x,double y){return x/viewport.scale>=toolbarX&&x/viewport.scale<toolbarX+toolbarWidth&&y/viewport.scale>=toolbarY&&y/viewport.scale<toolbarY+28;}
    private HandSide pick(double x,double y) {
        HandSide nearest=null;double score=Double.POSITIVE_INFINITY;
        for(var entry:HandRendering.bounds().entrySet())if(entry.getValue().contains(x,y)) {
            double next=entry.getValue().distance(x,y);
            if(next<score){nearest=entry.getKey();score=next;}
        }
        return nearest;
    }

    public boolean click(MouseButtonEvent event,boolean twice) {
        if(!eligible())return false;layout();
        if(toolbar(event.x(),event.y())) {
            for(var button:buttons)if(button.mouseClicked(adjusted(event),twice))return true;
            return true;
        }
        if(!dedicated&&event.y()>=owner.height-30)return false;
        if(event.button()!=InputConstants.MOUSE_BUTTON_LEFT&&event.button()!=InputConstants.MOUSE_BUTTON_RIGHT)return false;
        var side=pick(event.x(),event.y());if(side==null)return false;
        selected=side;dragButton=event.button();
        drag.begin(side,HandRendering.PROFILES.pose(ToolVisualsClient.CONFIG,side),HandRendering.bounds().get(side),event.x(),event.y(),event.button()==InputConstants.MOUSE_BUTTON_RIGHT);
        return true;
    }
    private static boolean precise(){return InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)||InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);}
    private static boolean shift(){return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)||InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);}

    public boolean move(MouseButtonEvent event) {
        if(!drag.dragging())return false;
        if(!eligible()){finish();return false;}
        HandProperty.change(ToolVisualsClient.CONFIG,drag.side(),drag.move(event.x(),event.y(),precise()));return true;
    }
    public boolean release(MouseButtonEvent event){if(!drag.dragging()||event.button()!=dragButton)return false;finish();return true;}
    public void finish(){drag.finish();}

    public boolean scroll(double x,double y,double vertical) {
        if(!eligible()||!Double.isFinite(vertical)||vertical==0)return false;layout();
        var side=drag.dragging()?drag.side():pick(x,y);
        if(side==null&&toolbar(x,y))side=selected;
        if(side==null||!dedicated&&y>=owner.height-30)return false;
        selected=side;var config=ToolVisualsClient.CONFIG;
        if(shift())HandProperty.change(config,side,Map.of(HandProperty.Z,config.value(HandProperty.Z.setting(side))+Math.clamp(vertical,-30,30)*(precise()?.01:.04)));
        else HandProperty.change(config,side,Map.of(HandProperty.SCALE,HandInteraction.scale(config.value(HandProperty.SCALE.setting(side)),vertical,precise())));
        return true;
    }

    public void paint(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial) {
        if(!eligible()){finish();return;}layout();
        var palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);
        var hovered=drag.dragging()?drag.side():pick(mouseX,mouseY);
        var side=hovered!=null?hovered:dedicated?selected:null;
        var box=side==null?null:HandRendering.bounds().get(side);
        if(box!=null) {
            int color=drag.dragging()?0xffffffff:palette.accentText;
            int x=Math.round(box.x()),y=Math.round(box.y()),w=Math.round(box.width()),h=Math.round(box.height());
            int corner=Math.min(12,Math.max(3,Math.min(w,h)/3));
            for(int dx:new int[]{0,1})for(int dy:new int[]{0,1}) {
                int cx=x+dx*w,cy=y+dy*h;
                graphics.fill(dx==0?cx:cx-corner,cy,dx==0?cx+corner:cx,cy+1,color);
                graphics.fill(cx,dy==0?cy:cy-corner,cx+1,dy==0?cy+corner:cy,color);
            }
            float size=viewport.scale*.75f;
            String label=sideName(side).getString()+" · "+String.format(Locale.ROOT,"%.2f×",ToolVisualsClient.CONFIG.value(HandProperty.SCALE.setting(side)));
            float width=UiPaint.textWidth(graphics,label,size)+12*viewport.scale;
            float left=Math.clamp(x,2,Math.max(2,owner.width-width-2)),top=Math.max(2,y-23*viewport.scale);
            UiPaint.rounded(graphics,left,top,width,20*viewport.scale,5*viewport.scale,palette.panel);
            UiPaint.centered(graphics,label,left,top,width,20*viewport.scale,size,palette.text,1);
        }
        long now=System.nanoTime();float elapsed=frame==0?0:Math.clamp((now-frame)/1_000_000_000f,0,.1f);frame=now;
        graphics.pose().pushMatrix();graphics.pose().scale(viewport.scale,viewport.scale);
        if(dedicated) {
            UiPaint.centered(graphics,text("drag_hint").getString(),0,toolbarY-48,viewport.width,20,.75f,palette.text,1);
            UiPaint.centered(graphics,text("more_hint").getString(),0,toolbarY-28,viewport.width,20,.72f,palette.muted,1);
        }
        for(var button:buttons){button.frame(palette,1,elapsed);button.extractRenderState(graphics,Math.round(mouseX/viewport.scale),Math.round(mouseY/viewport.scale),partial);}
        graphics.pose().popMatrix();
    }
}

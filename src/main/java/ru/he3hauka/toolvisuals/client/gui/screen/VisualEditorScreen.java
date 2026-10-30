package ru.he3hauka.toolvisuals.client.gui.screen;

import java.util.*;
import java.util.function.*;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.color.HexColorField;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.SettingsSaveIndicator;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public abstract class VisualEditorScreen extends Screen {
    protected final Screen parent;
    protected final List<AbstractWidget> controls = new ArrayList<>();
    protected UiPalette palette = new UiPalette(0);
    protected float scale;
    protected int px,py,frameWidth,frameHeight;
    private long lastFrame;
    protected VisualEditorScreen(Screen parent,Component title) { super(title);this.parent=parent; }
    protected static Component tr(String key,Object... args) { return UiI18n.tr("toolvisuals.editor."+key,args); }
    protected void frame(int w,int h) {
        controls.clear();frameWidth=w;frameHeight=h;
        var viewport=new UiViewport(width,height,minecraft.getWindow().getGuiScale());
        scale=Math.min(viewport.scale,Math.min(width/(w+24f),height/(h+24f)));
        px=Math.round((width/scale-w)/2);py=Math.round((height/scale-h)/2);lastFrame=System.nanoTime();
    }
    protected VisualsButton button(int x,int y,int w,int h,Component label,UiPaint.Icon icon,Runnable action,BooleanSupplier selected) {
        var b=addWidget(new VisualsButton(px+x,py+y,w,h,label,icon!=null&&label.getString().isEmpty()?VisualsButton.Kind.ICON:VisualsButton.Kind.CHIP,icon,Component.empty(),action,selected));controls.add(b);return b;
    }
    protected EditBox field(int x,int y,int w,String value,int max,Consumer<String> responder) {
        var f=addWidget(new EditBox(UiFont.get(),px+x,py+y,w,28,Component.empty()) {
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
                g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(getHeight()));super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
            }
        });f.setBordered(false);f.setTextShadow(false);f.setMaxLength(max);f.setValue(value);f.setResponder(responder);controls.add(f);return f;
    }
    protected HexColorField hexField(int x,int y,int width,String value,Consumer<String> responder) {
        var field=addWidget(new HexColorField(px+x,py+y,width,32,Component.literal("HEX"),value,null));
        field.setResponder(responder);controls.add(field);return field;
    }
    protected AbstractSliderButton slider(int x,int y,int w,Component name,double min,double max,double step,DoubleSupplier source,DoubleConsumer target,DoubleFunction<String> format) {
        var slider=addWidget(new AbstractSliderButton(px+x,py+y,w,32,name,(source.getAsDouble()-min)/(max-min)) {
            @Override protected void updateMessage() {}
            @Override protected void applyValue() { double next=Math.clamp(min+Math.round(value*(max-min)/step)*step,min,max);target.accept(next);value=(next-min)/(max-min); }
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
                isHovered=UiPaint.hovered(g,this,mx,my);
                handleCursor(g);
                String right=format.apply(source.getAsDouble());float reserved=UiPaint.textWidth(g,right,.8f)+12,a=active?1:.4f;
                UiPaint.baseline(g,UiPaint.fit(g,name,getWidth()-reserved,.8f),getX(),UiPaint.textBaseline(g,getY(),18,.8f),.8f,palette.muted,a);
                UiPaint.baseline(g,right,getX()+getWidth()-reserved+12,UiPaint.textBaseline(g,getY(),18,.8f),.8f,palette.text,a);
                UiPaint.rounded(g,getX()+4,getY()+24,getWidth()-8,3,1.5f,UiPaint.alpha(palette.track,a));
                UiPaint.rounded(g,getX()+4,getY()+24,(getWidth()-8)*(float)value,3,1.5f,UiPaint.alpha(palette.accent,a));
                UiPaint.rounded(g,getX()+(getWidth()-8)*(float)value,getY()+21,8,9,4,UiPaint.alpha(palette.accentText,a));
            }
        });controls.add(slider);return slider;
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial) { if(minecraft.level==null)extractPanorama(g,partial);g.fill(0,0,width,height,0xa0080910); }
    @Override public final void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);mx=Math.round(mx/scale);my=Math.round(my/scale);
        float elapsed=Math.clamp((System.nanoTime()-lastFrame)/1_000_000_000f,0,.1f);lastFrame=System.nanoTime();
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        UiPaint.shadow(g,px,py,frameWidth,frameHeight,12,1);UiPaint.rounded(g,px-1,py-1,frameWidth+2,frameHeight+2,13,palette.border);UiPaint.rounded(g,px,py,frameWidth,frameHeight,12,palette.panel);
        int reserved=ToolVisualsClient.CONFIG.persistenceStatus()==ru.he3hauka.toolvisuals.client.storage.JsonFile.Status.READY?0:150;
        UiPaint.baseline(g,UiPaint.fit(g,title,frameWidth-40-reserved,1.05f),px+20,UiPaint.textBaseline(g,py+12,28,1.05f),1.05f,palette.text,1);
        SettingsSaveIndicator.draw(g,ToolVisualsClient.CONFIG,px+frameWidth-160,py+12,140,palette,1);
        g.fill(px+20,py+49,px+frameWidth-20,py+50,palette.border);
        paint(g,mx-px,my-py);
        for(var widget:controls) {
            if(widget instanceof VisualsButton b)b.frame(palette,1,elapsed);
            if(widget instanceof HexColorField field)field.frame(palette);
            else if(widget instanceof EditBox f){UiPaint.rounded(g,f.getX()-6,f.getY(),f.getWidth()+12,f.getHeight(),6,palette.field);f.setTextColor(fieldColor(f));}
            widget.extractRenderState(g,mx,my,partial);
        }
        overlay(g);
        g.pose().popMatrix();
    }
    protected abstract void paint(GuiGraphicsExtractor g,int mx,int my);
    protected void overlay(GuiGraphicsExtractor g) {}
    protected int fieldColor(EditBox field) { return palette.text; }
    protected MouseButtonEvent adjusted(MouseButtonEvent e) { return new MouseButtonEvent(e.x()/scale,e.y()/scale,e.buttonInfo()); }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){return super.mouseClicked(adjusted(e),twice);}
    @Override public boolean mouseReleased(MouseButtonEvent e){return super.mouseReleased(adjusted(e));}
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){return super.mouseDragged(adjusted(e),dx/scale,dy/scale);}
    @Override public void mouseMoved(double x,double y){super.mouseMoved(x/scale,y/scale);}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}

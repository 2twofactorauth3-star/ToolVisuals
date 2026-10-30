package ru.he3hauka.toolvisuals.client.utility.ui;

import java.util.*;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public abstract class UtilityScreen extends VisualEditorScreen {
    @FunctionalInterface protected interface WriteAction {void run() throws java.io.IOException;}
    protected void persist(WriteAction write,Runnable success) {
        try {write.run();success.run();}
        catch(java.io.IOException error){status=ui("io_error").getString();}
        catch(IllegalArgumentException|IllegalStateException error){status=ui("storage_limit").getString();}
    }
    protected void preferencesStatus() {
        var state=ru.he3hauka.toolvisuals.client.ToolVisualsClient.UTILITIES.status();
        if(state==ru.he3hauka.toolvisuals.client.storage.JsonFile.Status.LOAD_FAILED||state==ru.he3hauka.toolvisuals.client.storage.JsonFile.Status.WRITE_FAILED)status=ui("io_error").getString();
    }
    protected final List<AbstractWidget> listControls=new ArrayList<>();
    protected int page;
    protected String status="";
    protected UtilityScreen(Screen parent,String title){super(parent,ui(title));}
    protected static Component ui(String key,Object... args){return UiI18n.tr("toolvisuals.utility."+key,args);}
    protected void begin(){listControls.clear();frame(620,440);button(474,396,122,28,ui("back"),UiPaint.Icon.BACK,this::onClose,null);}
    protected void clearRows(){for(var widget:listControls){removeWidget(widget);controls.remove(widget);}listControls.clear();}
    protected VisualsButton row(int index,int x,int width,Component label,Runnable action) {
        var widget=button(x,114+index*40,width,36,label,null,action,null);listControls.add(widget);return widget;
    }
    protected void pages(int count,Runnable refresh) {
        int maximum=Math.max(0,(count-1)/7);page=Math.clamp(page,0,maximum);
        var previous=button(24,396,32,28,Component.literal("‹"),null,()->{page--;refresh.run();},null);
        var next=button(60,396,32,28,Component.literal("›"),null,()->{page++;refresh.run();},null);
        previous.active=page>0;next.active=page<maximum;listControls.add(previous);listControls.add(next);
    }
    protected void label(GuiGraphicsExtractor g,String text,int x,int y,int width,float size,int color){UiPaint.baseline(g,UiPaint.fit(g,Component.literal(text),width,size),px+x,UiPaint.textBaseline(g,py+y,24,size),size,color,1);}
    protected void footer(GuiGraphicsExtractor g){label(g,status,104,398,358,.72f,palette.muted);}
    protected void attempt(Runnable action){try{action.run();status=ui("done").getString();}catch(Exception error){status=ui("failed").getString();org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Utility action failed",error);}}
}

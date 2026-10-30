package ru.he3hauka.toolvisuals.client.cosmetic.outfit.ui;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.cosmetic.outfit.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;

public final class OutfitScreen extends VisualEditorScreen {
    private final Supplier<AccessoryLook> capture;
    private final Consumer<AccessoryLook> applied;
    private int page;
    private UUID editing;
    private String name="",status="";

    public OutfitScreen(Screen parent){this(parent,()->ToolVisualsClient.CONFIG.accessories(),look->{});}
    public OutfitScreen(Screen parent,Supplier<AccessoryLook> capture,Consumer<AccessoryLook> applied){super(parent,text("title"));this.capture=capture;this.applied=applied;}
    private static Component text(String key,Object... args){return UiI18n.tr("toolvisuals.outfits."+key,args);}
    @Override protected void init(){
        frame(700,500);var entries=ToolVisualsClient.OUTFITS.entries();page=Math.clamp(page,0,Math.max(0,(entries.size()-1)/6));
        field(26,66,324,name,32,value->name=value);
        button(366,66,104,28,text("new"),UiPaint.Icon.PLUS,()->{editing=null;name="";rebuildWidgets();},null);
        if(editing==null)button(480,66,194,28,text("save"),UiPaint.Icon.CHECK,this::save,null);
        else {
            button(480,66,90,28,text("rename"),null,this::rename,null);
            button(578,66,96,28,text("replace"),UiPaint.Icon.CHECK,this::save,null);
        }
        for(int i=0;i<6&&page*6+i<entries.size();i++){
            var entry=entries.get(page*6+i);int y=112+i*44;
            button(26,y,322,36,Component.literal(entry.name()),null,()->{editing=entry.id();name=entry.name();rebuildWidgets();},()->entry.id().equals(editing));
            button(358,y,152,36,text("wear"),UiPaint.Icon.CHECK,()->{try{Outfits.wear(entry);applied.accept(entry.look());status=text("equipped").getString();}catch(IOException error){status=text("io_error").getString();}catch(IllegalArgumentException error){status=text("cape_limit").getString();}},()->Outfits.wearing(entry));
            button(520,y,154,36,text("delete"),UiPaint.Icon.CLOSE,()->minecraft.gui.setScreen(new ConfirmScreen(accepted->{minecraft.gui.setScreen(this);if(accepted)try{ToolVisualsClient.OUTFITS.delete(entry.id());if(entry.id().equals(editing)){editing=null;name="";}status=text("deleted").getString();rebuildWidgets();}catch(IOException error){status=text("io_error").getString();}},text("delete"),Component.literal(entry.name()))),null);
        }
        button(26,392,32,28,Component.literal("‹"),null,()->{page--;rebuildWidgets();},null).active=page>0;
        button(66,392,32,28,Component.literal("›"),null,()->{page++;rebuildWidgets();},null).active=(page+1)*6<entries.size();
        button(526,450,148,28,UiI18n.tr("toolvisuals.ui.back"),UiPaint.Icon.BACK,this::onClose,null);
    }
    private void save(){
        Runnable persist=()->{try{var cape=ToolVisualsClient.CAPES.selected();var entry=ToolVisualsClient.OUTFITS.put(editing,name,capture.get(),cape==null?null:cape.design());editing=entry.id();name=entry.name();status=text("saved").getString();rebuildWidgets();}catch(IOException error){status=text("io_error").getString();}catch(IllegalArgumentException error){status=text("invalid",OutfitStore.LIMIT).getString();}};
        if(editing==null)persist.run();else minecraft.gui.setScreen(new ConfirmScreen(accepted->{minecraft.gui.setScreen(this);if(accepted)persist.run();},text("replace_confirm"),Component.literal(name)));
    }
    private void rename(){
        var selected=ToolVisualsClient.OUTFITS.entries().stream().filter(entry->entry.id().equals(editing)).findFirst();if(selected.isEmpty())return;
        try{var entry=selected.get();var saved=ToolVisualsClient.OUTFITS.put(entry.id(),name,entry.look(),entry.cape());name=saved.name();status=text("saved").getString();rebuildWidgets();}
        catch(IOException error){status=text("io_error").getString();}catch(IllegalArgumentException error){status=text("invalid",OutfitStore.LIMIT).getString();}
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        String message=status.isEmpty()?text("hint",ToolVisualsClient.OUTFITS.entries().size(),OutfitStore.LIMIT).getString():status;
        UiPaint.baseline(g,UiPaint.fit(g,Component.literal(message),648,.75f),px+26,UiPaint.textBaseline(g,py+426,20,.75f),.75f,palette.muted,1);
        if(ToolVisualsClient.OUTFITS.entries().isEmpty())UiPaint.centered(g,text("empty").getString(),px+26,py+190,648,60,.9f,palette.muted,1);
    }
}

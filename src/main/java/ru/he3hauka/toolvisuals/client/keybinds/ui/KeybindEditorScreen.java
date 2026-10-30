package ru.he3hauka.toolvisuals.client.keybinds.ui;

import java.util.*;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.keybinds.*;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

import static ru.he3hauka.toolvisuals.client.keybinds.ui.KeybindsScreen.text;

public final class KeybindEditorScreen extends VisualEditorScreen {
    private final UUID id;
    private final boolean enabled;
    private String name,key,value,status="";
    private ActionBind.Kind kind;
    private boolean listening;
    private List<String> approved=List.of();
    public KeybindEditorScreen(Screen parent,ActionBind bind){
        super(parent,text("editor"));id=bind==null?UUID.randomUUID():bind.id();enabled=bind==null||bind.enabled();name=bind==null?"":bind.name();key=bind==null?"key.keyboard.unknown":bind.key();value=bind==null?"":bind.value();kind=bind==null?ActionBind.Kind.COMMAND:bind.kind();
        if(bind!=null)approved=BindingConflicts.find(InputConstants.getKey(key),null,id);
    }
    @Override protected void init(){
        frame(620,400);
        field(30,92,554,name,48,next->name=next);
        button(24,146,278,36,text("kind_"+kind.name().toLowerCase(Locale.ROOT)),null,()->{kind=ActionBind.Kind.values()[(kind.ordinal()+1)%ActionBind.Kind.values().length];rebuildWidgets();},null);
        button(318,146,278,36,listening?text("press_key"):KeybindsScreen.keyLabel(key),UiPaint.Icon.KEYBOARD,()->{listening=true;setFocused(null);rebuildWidgets();},()->listening);
        field(30,232,554,value,256,next->value=next);
        button(24,350,278,30,text("cancel"),UiPaint.Icon.BACK,this::onClose,null);
        button(318,350,278,30,text("save"),UiPaint.Icon.CHECK,this::save,null);
    }
    private void save(){
        final ActionBind bind;
        try{bind=new ActionBind(id,name,key,kind,value,enabled);InputConstants.getKey(key);}catch(RuntimeException error){status=text("invalid").getString();return;}
        var input=InputConstants.getKey(key);var conflicts=BindingConflicts.find(input,null,id);
        Runnable write=()->{try{Keybinds.STORE.put(bind);onClose();}catch(java.io.IOException error){status=text("io_error").getString();}catch(IllegalStateException error){status=text("limit").getString();}};
        if(approved.containsAll(conflicts))write.run();else BindingConflicts.confirm(this,input,conflicts,()->{approved=conflicts;write.run();});
    }
    private void assign(InputConstants.Key input){
        listening=false;var conflicts=BindingConflicts.find(input,null,id);
        BindingConflicts.confirm(this,input,conflicts,()->{key=input.getName();approved=conflicts;rebuildWidgets();});rebuildWidgets();
    }
    @Override public boolean keyPressed(KeyEvent event){
        if(!listening)return super.keyPressed(event);
        if(event.key()==InputConstants.KEY_ESCAPE){listening=false;rebuildWidgets();return true;}
        assign(event.key()==InputConstants.KEY_BACKSPACE||event.key()==InputConstants.KEY_DELETE?InputConstants.UNKNOWN:InputConstants.getKey(event));return true;
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){if(listening){assign(InputConstants.Type.MOUSE.getOrCreate(event.button()));return true;}return super.mouseClicked(event,twice);}
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        label(g,text("name"),24,62,572);label(g,text(kind==ActionBind.Kind.MODULE?"module_hint":"value"),24,198,572);
        String detail=kind==ActionBind.Kind.MODULE?Arrays.stream(VisualModule.values()).filter(module->module.id.equalsIgnoreCase(value)||module.title.equalsIgnoreCase(value)).map(module->module.title).findFirst().orElse(text("module_example").getString()):text("shared_hint").getString();
        label(g,Component.literal(detail),24,276,572);label(g,Component.literal(status),24,310,572);
    }
    private void label(GuiGraphicsExtractor g,Component label,int x,int y,int width){UiPaint.baseline(g,UiPaint.fit(g,label,width,.78f),px+x,UiPaint.textBaseline(g,py+y,24,.78f),.78f,palette.muted,1);}
}

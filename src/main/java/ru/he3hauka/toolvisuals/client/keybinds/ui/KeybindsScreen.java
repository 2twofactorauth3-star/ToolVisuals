package ru.he3hauka.toolvisuals.client.keybinds.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.keybinds.*;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.utility.ui.UtilityScreen;

public final class KeybindsScreen extends UtilityScreen {
    public KeybindsScreen(Screen parent){super(parent,"action_binds");}
    static Component text(String key,Object... args){return UiI18n.tr("toolvisuals.keybinds."+key,args);}
    @Override protected void init(){
        begin();var config=ToolVisualsClient.CONFIG;
        button(24,66,164,30,text("enabled"),UiPaint.Icon.KEYBOARD,()->config.setActive(VisualModule.KEYBINDS,!config.active(VisualModule.KEYBINDS)),()->config.active(VisualModule.KEYBINDS));
        button(198,66,192,30,text("vanilla"),null,()->minecraft.gui.setScreen(new KeyBindsScreen(this,minecraft.options)),null);
        button(400,66,196,30,text("add"),UiPaint.Icon.PLUS,()->minecraft.gui.setScreen(new KeybindEditorScreen(this,null)),null).active=Keybinds.STORE.entries().size()<KeybindStore.LIMIT;
        refresh();
    }
    private void refresh(){
        clearRows();var entries=Keybinds.STORE.entries();pages(entries.size(),this::refresh);
        for(int index=0;index<7&&page*7+index<entries.size();index++){
            var bind=entries.get(page*7+index);int y=114+index*40;
            var edit=row(index,24,330,Component.literal(bind.name()),()->minecraft.gui.setScreen(new KeybindEditorScreen(this,bind)));
            edit.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(bind.value())));
            var toggle=button(366,y,180,36,bind.enabled()?keyLabel(bind.key()):text("disabled"),null,()->persist(()->Keybinds.STORE.put(bind.enabled(!bind.enabled())),this::refresh),bind::enabled);listControls.add(toggle);
            listControls.add(button(558,y,38,36,Component.empty(),UiPaint.Icon.CLOSE,()->minecraft.gui.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(yes->{minecraft.gui.setScreen(this);if(yes)persist(()->Keybinds.STORE.remove(bind.id()),()->minecraft.gui.setScreen(this));},text("delete"),Component.literal(bind.name()))),null));
        }
        status=Keybinds.STORE.status()==ru.he3hauka.toolvisuals.client.storage.JsonFile.Status.LOAD_FAILED?text("io_error").getString():text("count",entries.size(),KeybindStore.LIMIT).getString();
    }
    static Component keyLabel(String key){try{return com.mojang.blaze3d.platform.InputConstants.getKey(key).getDisplayName();}catch(RuntimeException error){return Component.literal(key);}}
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){footer(g);}
}

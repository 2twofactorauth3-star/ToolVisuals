package ru.he3hauka.toolvisuals.client.utility.packs.ui;

import java.util.*;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.utility.ui.UtilityScreen;

public final class PackSetsScreen extends UtilityScreen {
    private final ResourcePacksScreen packs;
    private EditBox name;
    private String draft="";
    public PackSetsScreen(ResourcePacksScreen parent){super(parent,"pack_sets");packs=parent;}
    @Override protected void init(){begin();preferencesStatus();name=field(30,66,420,draft,48,text->draft=text);button(472,66,124,28,ui("save_set"),null,this::save,null);refresh();}
    private void save() {
        String value=name.getValue().strip();if(value.isEmpty()){status=ui("name_required").getString();return;}
        Runnable write=()->persist(()->ToolVisualsClient.UTILITIES.storePacks(value,packs.selected()),()->{status=ui("done").getString();refresh();});
        if(ToolVisualsClient.UTILITIES.packSets().containsKey(value))minecraft.gui.setScreen(new ConfirmScreen(accepted->{minecraft.gui.setScreen(this);if(accepted)write.run();},ui("overwrite_set"),Component.literal(value)));else write.run();
    }
    private void refresh() {
        clearRows();var sets=new ArrayList<>(ToolVisualsClient.UTILITIES.packSets().entrySet());pages(sets.size(),this::refresh);
        for(int i=0;i<7&&page*7+i<sets.size();i++) {
            var set=sets.get(page*7+i);row(i,24,524,Component.literal(set.getKey()+"  ·  "+set.getValue().size()),()->{packs.useSet(set.getValue());minecraft.gui.setScreen(packs);});
            var remove=button(560,114+i*40,36,36,Component.empty(),UiPaint.Icon.CLOSE,()->minecraft.gui.setScreen(new ConfirmScreen(accepted->{minecraft.gui.setScreen(this);if(accepted)persist(()->ToolVisualsClient.UTILITIES.removePacks(set.getKey()),this::refresh);},ui("delete_set"),Component.literal(set.getKey()))),null);listControls.add(remove);
        }
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){footer(g);}
}

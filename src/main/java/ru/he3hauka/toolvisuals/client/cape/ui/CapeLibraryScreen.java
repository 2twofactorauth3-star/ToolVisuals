package ru.he3hauka.toolvisuals.client.cape.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.cape.CapeStore;
import ru.he3hauka.toolvisuals.client.cape.CapeTexture;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;

final class CapeLibraryScreen extends VisualEditorScreen {
    private final CapeEditorScreen editor;
    private int page;
    private final Map<java.util.UUID,CapeTexture> previews=new HashMap<>();
    private String status="";
    CapeLibraryScreen(CapeEditorScreen parent){super(parent,CapeEditorScreen.text("library"));editor=parent;}
    @Override protected void init(){
        frame(660,454);var entries=ToolVisualsClient.CAPES.entries();page=Math.clamp(page,0,Math.max(0,(entries.size()-1)/7));
        var selected=ToolVisualsClient.CAPES.selected();
        for(int i=0;i<7&&page*7+i<entries.size();i++){
            var entry=entries.get(page*7+i);int y=68+i*44;
            button(56,y,312,36,Component.literal(entry.name()),selected!=null&&selected.id().equals(entry.id())?UiPaint.Icon.CHECK:null,()->{minecraft.gui.setScreen(editor);editor.choose(entry);},null);
            button(378,y,120,36,CapeEditorScreen.text("wear"),null,()->persist(()->ToolVisualsClient.CAPES.select(entry.id())),()->ToolVisualsClient.CAPES.selected()!=null&&ToolVisualsClient.CAPES.selected().id().equals(entry.id()));
            button(508,y,128,36,CapeEditorScreen.text("delete"),UiPaint.Icon.CLOSE,()->minecraft.gui.setScreen(new ConfirmScreen(accepted->{minecraft.gui.setScreen(this);if(accepted)persist(()->{ToolVisualsClient.CAPES.delete(entry.id());editor.removedVariant(entry.id());});},CapeEditorScreen.text("delete_confirm"),Component.literal(entry.name()))),null);
        }
        button(24,390,32,28,Component.literal("‹"),null,()->{page--;rebuildWidgets();},null).active=page>0;
        button(64,390,32,28,Component.literal("›"),null,()->{page++;rebuildWidgets();},null).active=(page+1)*7<entries.size();
        button(108,390,246,28,CapeEditorScreen.text("no_cape"),null,()->persist(()->ToolVisualsClient.CAPES.select(null)),null);
        button(488,414,148,28,CapeEditorScreen.text("return_editor"),UiPaint.Icon.BACK,this::onClose,null);
    }
    @FunctionalInterface private interface Save {void run() throws IOException;}
    private void persist(Save action){try{action.run();status=CapeEditorScreen.text("saved").getString();rebuildWidgets();}catch(IOException error){status=CapeEditorScreen.text("io_error").getString();}}
    @Override public void removed(){previews.values().forEach(CapeTexture::close);previews.clear();super.removed();}
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        var entries=ToolVisualsClient.CAPES.entries();
        for(int i=0;i<7&&page*7+i<entries.size();i++){
            var entry=entries.get(page*7+i);var texture=previews.computeIfAbsent(entry.id(),id->new CapeTexture(true));texture.update(entry.design());
            int y=py+68+i*44;UiPaint.rounded(g,px+24,y,24,36,4,palette.field);
            if(texture.ready())g.blit(RenderPipelines.GUI_TEXTURED,texture.id,px+26,y+2,2,2,20,32,20,32,128,64,0xffffffff);
        }
        String message=status.isEmpty()?CapeEditorScreen.text("library_count",ToolVisualsClient.CAPES.entries().size(),CapeStore.LIMIT).getString():status;
        if(ToolVisualsClient.CAPES.entries().isEmpty())UiPaint.centered(g,CapeEditorScreen.text("empty").getString(),px+24,py+160,612,50,.9f,palette.muted,1);
        UiPaint.baseline(g,UiPaint.fit(g,Component.literal(message),440,.74f),px+24,UiPaint.textBaseline(g,py+418,24,.74f),.74f,palette.muted,1);
    }
}

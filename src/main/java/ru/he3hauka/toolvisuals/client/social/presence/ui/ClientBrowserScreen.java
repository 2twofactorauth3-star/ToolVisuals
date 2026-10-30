package ru.he3hauka.toolvisuals.client.social.presence.ui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;
import ru.he3hauka.toolvisuals.client.social.presence.*;
import ru.he3hauka.toolvisuals.client.social.profile.PlayerCardScreen;

public final class ClientBrowserScreen extends VisualEditorScreen {
    private List<PresenceRows.Entry> entries=List.of();
    private int page;
    public ClientBrowserScreen(Screen parent){super(parent,UiI18n.tr("toolvisuals.client_list.open"));}
    @Override protected void init(){
        frame(640,500);entries=ClientListClient.entries();page=Math.clamp(page,0,Math.max(0,(entries.size()-1)/7));
        for(int i=0;i<7&&page*7+i<entries.size();i++){
            var entry=entries.get(page*7+i);button(26,68+i*44,588,36,Component.literal(NameProtect.text(entry.name())),UiPaint.Icon.PLAYER,()->minecraft.gui.setScreen(new PlayerCardScreen(this,entry.id(),entry.name())),null);
        }
        button(26,392,32,28,Component.literal("‹"),null,()->{page--;rebuildWidgets();},null).active=page>0;
        button(66,392,32,28,Component.literal("›"),null,()->{page++;rebuildWidgets();},null).active=(page+1)*7<entries.size();
        button(110,392,236,28,UiI18n.tr("toolvisuals.profile.self"),UiPaint.Icon.PLAYER,()->{if(minecraft.player!=null)minecraft.gui.setScreen(new PlayerCardScreen(this,minecraft.player.getUUID(),minecraft.player.getGameProfile().name()));},null).active=minecraft.player!=null;
        button(466,450,148,28,UiI18n.tr("toolvisuals.ui.back"),UiPaint.Icon.BACK,this::onClose,null);
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        if(entries!=ClientListClient.entries())rebuildWidgets();
        var key=minecraft.player==null?"toolvisuals.profile.join":"toolvisuals.client_list.open_hint";
        UiPaint.baseline(g,UiPaint.fit(g,UiI18n.tr(key),588,.74f),px+26,UiPaint.textBaseline(g,py+426,20,.74f),.74f,palette.muted,1);
        if(entries.isEmpty())UiPaint.centered(g,UiI18n.tr("toolvisuals.client_list.empty").getString(),px+26,py+190,588,60,.9f,palette.muted,1);
    }
}

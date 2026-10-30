package ru.he3hauka.toolvisuals.client.social.presence.ui;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.hud.players.PlayerListPanel;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.social.presence.*;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ClientListHud {
    private static final List<PlayerListPanel.Row> PREVIEW=List.of(
            new PlayerListPanel.Row("PlayerOne","",0,true),new PlayerListPanel.Row("PlayerTwo","",0,true));
    private static List<PresenceRows.Entry> source;
    private static List<PlayerListPanel.Row> rows=List.of();
    private ClientListHud(){}
    public static boolean openAt(double x,double y){
        var client=net.minecraft.client.Minecraft.getInstance();
        if(!(client.gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen)||!ToolVisualsClient.CONFIG.active(ru.he3hauka.toolvisuals.client.module.VisualModule.CLIENT_LIST)
                ||!HudPlacement.bounds().containsKey(HudElement.CLIENTS)||source==null)return false;
        int row=PlayerListPanel.rowAt(HudElement.CLIENTS,x,y);if(row<0||row>=source.size())return false;
        var entry=source.get(row);client.gui.setScreen(new ru.he3hauka.toolvisuals.client.social.profile.PlayerCardScreen(client.gui.screen(),entry.id(),entry.name()));return true;
    }

    public static void draw(GuiGraphicsExtractor graphics,UiPalette palette){
        var entries=ClientListClient.entries();
        if(source!=entries){rows=entries.stream().map(entry->new PlayerListPanel.Row(entry.name(),"",0,true)).toList();source=entries;}
        var shown=HudPlacement.samples()||rows.isEmpty()&&HudPlacement.preview()?PREVIEW:rows;
        var origin=ClientListClient.source();
        String title=origin!=ClientListClient.Source.SERVER&&!HudPlacement.samples()?"title_nearby":"title";
        String empty=switch(origin){case NEARBY->"requires_plugin";case SYNCING->"syncing";case SERVER->"empty";};
        PlayerListPanel.draw(graphics,palette,HudElement.CLIENTS,CLIENT_LIST_SCALE,shown,UiI18n.tr("toolvisuals.client_list."+title),
                UiI18n.tr("toolvisuals.client_list."+empty),Integer.toString(shown.size()),
                (int)ToolVisualsClient.CONFIG.value(CLIENT_LIST_NAME_LIMIT),"toolvisuals.client_list.more",UiPaint.Icon.RENDER);
    }
}

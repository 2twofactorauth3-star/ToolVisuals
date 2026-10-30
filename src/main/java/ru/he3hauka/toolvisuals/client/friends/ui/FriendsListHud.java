package ru.he3hauka.toolvisuals.client.friends.ui;

import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.friends.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.hud.players.PlayerListPanel;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class FriendsListHud {
    private static long revision=-1,mappedRevision=-1;
    private static Set<String> players;
    private static boolean onlyOnline;
    private static UiLanguage language;
    private static List<FriendPresence.Entry> entries=List.of(),mappedSource;
    private static List<PlayerListPanel.Row> mapped=List.of();
    private static final List<FriendPresence.Entry> PREVIEW=List.of(
            new FriendPresence.Entry(new FriendStore.Friend("PlayerOne",0x77caff),true),
            new FriendPresence.Entry(new FriendStore.Friend("PlayerTwo",0xb09bff),true));
    private FriendsListHud(){}

    private static List<FriendPresence.Entry> entries(){
        var store=ToolVisualsClient.FRIENDS;var nextPlayers=Friends.onlineNames();
        boolean nextOnlyOnline=ToolVisualsClient.CONFIG.flag(FRIENDS_LIST_ONLINE_ONLY);
        if(revision!=store.revision()||players!=nextPlayers||onlyOnline!=nextOnlyOnline){
            entries=FriendPresence.list(store.entries(),nextPlayers,nextOnlyOnline);
            revision=store.revision();players=nextPlayers;onlyOnline=nextOnlyOnline;
        }
        return entries;
    }

    public static void draw(GuiGraphicsExtractor g,UiPalette palette){
        var config=ToolVisualsClient.CONFIG;var rows=entries();
        if(rows.isEmpty()&&HudPlacement.preview())rows=PREVIEW;
        var store=ToolVisualsClient.FRIENDS;
        if(mappedSource!=rows||mappedRevision!=store.revision()||language!=config.language()){
            String online=UiI18n.tr("toolvisuals.friends.online").getString(),offline=UiI18n.tr("toolvisuals.friends.offline").getString();
            mapped=rows.stream().map(row->new PlayerListPanel.Row(row.friend.name,row.online?online:offline,0xff000000|store.color(row.friend),row.online)).toList();
            mappedSource=rows;mappedRevision=store.revision();language=config.language();
        }
        int onlineCount=0;for(var row:rows)if(row.online)onlineCount++;
        String count=onlyOnline?Integer.toString(rows.size()):onlineCount+" / "+rows.size();
        PlayerListPanel.draw(g,palette,HudElement.FRIENDS,FRIENDS_LIST_SCALE,mapped,UiI18n.tr("toolvisuals.friends_list.title"),
                UiI18n.tr(onlyOnline?"toolvisuals.friends_list.empty_online":"toolvisuals.friends_list.empty"),count,
                (int)config.value(FRIENDS_LIST_NAME_LIMIT),"toolvisuals.friends_list.more",UiPaint.Icon.PLAYER);
    }
}

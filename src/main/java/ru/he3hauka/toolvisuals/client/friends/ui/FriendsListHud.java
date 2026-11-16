package ru.he3hauka.toolvisuals.client.friends.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.compatibility.input.MouseCoordinates;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.friends.*;
import ru.he3hauka.toolvisuals.client.friends.network.FriendDataClient;
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
    private static long dataRevision=-1;
    private static int preferences=-1;
    private static Set<String> players;
    private static boolean onlyOnline;
    private static UiLanguage language;
    private static List<FriendPresence.Entry> entries=List.of(),mappedSource;
    private static List<PlayerListPanel.Row> mapped=List.of();
    private static final List<FriendPresence.Entry> PREVIEW=List.of(
            new FriendPresence.Entry(new FriendStore.Friend("PlayerOne",0x77caff),true),
            new FriendPresence.Entry(new FriendStore.Friend("PlayerTwo",0xb09bff),true));
    private static final class Sample {
        static FriendData data(){
            var inventory=new ArrayList<ItemStack>(Collections.nCopies(37,ItemStack.EMPTY));
            inventory.set(9,new ItemStack(Items.ENDER_PEARL,16));inventory.set(10,new ItemStack(Items.GOLDEN_APPLE,4));
            return new FriendData(true,18,20,4,List.of(new ItemStack(Items.DIAMOND_HELMET),new ItemStack(Items.DIAMOND_CHESTPLATE),new ItemStack(Items.DIAMOND_LEGGINGS),new ItemStack(Items.DIAMOND_BOOTS)),inventory);
        }
        static final FriendData DATA=data();
    }
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
        int nextPreferences=(config.flag(FRIENDS_LIST_HEALTH)?1:0)|(config.flag(FRIENDS_LIST_ARMOR)?2:0)|(config.flag(FRIENDS_LIST_INVENTORY)?4:0)|((int)config.value(FRIENDS_LIST_INVENTORY_MODE)<<3)|(HudPlacement.samples()||rows==PREVIEW?16:0);
        long nextData=FriendDataClient.revision();
        if(mappedSource!=rows||mappedRevision!=store.revision()||language!=config.language()||nextData!=dataRevision||nextPreferences!=preferences){
            String online=UiI18n.tr("toolvisuals.friends.online").getString(),offline=UiI18n.tr("toolvisuals.friends.offline").getString();
            mapped=rows.stream().map(row->{
                var settings=row.friend.display;var data=(nextPreferences&16)!=0?Sample.DATA:FriendDataClient.data(row.friend.name);
                var details=row.online?new FriendRowDetails(data,config.flag(FRIENDS_LIST_HEALTH)&&settings.health,config.flag(FRIENDS_LIST_ARMOR)&&settings.armor,config.flag(FRIENDS_LIST_INVENTORY)&&settings.inventory,settings.mode((int)config.value(FRIENDS_LIST_INVENTORY_MODE))):null;
                return new PlayerListPanel.Row(row.friend.name,row.online?"":offline,0xff000000|store.color(row.friend),row.online,details);
            }).toList();
            mappedSource=rows;mappedRevision=store.revision();language=config.language();dataRevision=nextData;preferences=nextPreferences;
        }
        int onlineCount=0;for(var row:rows)if(row.online)onlineCount++;
        String count=onlyOnline?Integer.toString(rows.size()):onlineCount+" / "+rows.size();
        PlayerListPanel.draw(g,palette,HudElement.FRIENDS,FRIENDS_LIST_SCALE,mapped,UiI18n.tr("toolvisuals.friends_list.title"),
                UiI18n.tr(onlyOnline?"toolvisuals.friends_list.empty_online":"toolvisuals.friends_list.empty"),count,
                (int)config.value(FRIENDS_LIST_NAME_LIMIT),"toolvisuals.friends_list.more",UiPaint.Icon.PLAYER);
        var client=Minecraft.getInstance();
        String hovered=null;
        if(client.gui.screen() instanceof ChatScreen){
            double mouseX=MouseCoordinates.x(client),mouseY=MouseCoordinates.y(client);
            int index=PlayerListPanel.rowAt(HudElement.FRIENDS,mouseX,mouseY);
            if(index>=0&&index<mapped.size()&&mapped.get(index).details() instanceof FriendRowDetails details&&details.inventory){
                hovered=mapped.get(index).name();
                if(details.mode==1){float scale=Math.clamp(client.getWindow().getHeight()/720f,1,2)*config.number(FRIENDS_LIST_SCALE)/client.getWindow().getGuiScale();details.popup(g,palette,mouseX,mouseY,scale,hovered);}
            }
        }
        FriendDataClient.hover(hovered);
    }
}

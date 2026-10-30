package ru.he3hauka.toolvisuals.client.friends;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.player.ClientActions;

public final class Friends {
    private static Object connection;
    private static Map<UUID,String> online = Map.of();
    private static Set<String> onlineNames = Set.of();
    private static int ticks;
    private Friends() {}
    public static boolean online(String name) { return name!=null&&onlineNames.contains(name.toLowerCase(Locale.ROOT)); }
    public static Set<String> onlineNames() { return onlineNames; }
    public static void tick(Minecraft client) {
        var current=client.getConnection();
        if(current!=connection) { connection=current;online=Map.of();onlineNames=Set.of();ticks=0; }
        if(current==null||client.level==null) { online=Map.of();onlineNames=Set.of();return; }
        if(++ticks%10!=0)return;
        var next=ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster.online(client);
        if(ticks>60) {
            for(var entry:next.entrySet())if(!online.containsKey(entry.getKey()))notice(entry.getValue(),true);
            for(var entry:online.entrySet())if(!next.containsKey(entry.getKey()))notice(entry.getValue(),false);
        }
        if(!online.equals(next)) {
            var names=new HashSet<String>();
            for(var name:next.values())names.add(name.toLowerCase(Locale.ROOT));
            onlineNames=Set.copyOf(names);online=next;
        }
    }
    private static void notice(String name,boolean joined) {
        var store=ToolVisualsClient.FRIENDS;var friend=store.get(name);var options=store.settings();
        if(friend==null||!(joined?options.join:options.leave))return;
        var nick=Component.literal(friend.name).withStyle(style->style.withColor(store.color(friend)));
        ClientActions.notice(joined?"friends.joined":"friends.left",nick);
    }
}

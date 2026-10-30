package ru.he3hauka.toolvisuals.client.optimization.hud;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

public final class PlayerRoster {
    private static long membership,display;
    private static final RevisionValue<Map<UUID,String>> ONLINE=new RevisionValue<>(),LISTED=new RevisionValue<>();
    private PlayerRoster(){}
    public static void initialize(){net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->reset());}
    private static void reset(){ONLINE.clear();LISTED.clear();membershipChanged();}
    public static void info(ClientboundPlayerInfoUpdatePacket packet){
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;
        var actions=packet.actions();
        if(actions.size()==1&&actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY))return;
        membership++;display++;
    }
    public static void membershipChanged(){if(!ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active()){membership++;display++;}}
    public static void presentationChanged(){if(!ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())display++;}
    public static long presentation(){return display;}
    public static Map<UUID,String> online(Minecraft client){return names(client,false);}
    public static Map<UUID,String> listedOnline(Minecraft client){return names(client,true);}
    private static Map<UUID,String> names(Minecraft client,boolean listed){
        var connection=client.getConnection();if(connection==null)return Map.of();
        return (listed?LISTED:ONLINE).get(connection,membership,()->{
            var names=new HashMap<UUID,String>();
            for(var info:listed?connection.getListedOnlinePlayers():connection.getOnlinePlayers())names.put(info.getProfile().id(),info.getProfile().name());
            return Map.copyOf(names);
        });
    }
}

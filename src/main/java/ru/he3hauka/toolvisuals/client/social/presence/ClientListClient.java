package ru.he3hauka.toolvisuals.client.social.presence;

import java.util.*;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.animation.PlaybackClient;
import ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster;
import ru.he3hauka.toolvisuals.client.social.SocialClient;
import ru.he3hauka.toolvisuals.client.social.presence.network.PresencePayload;
import ru.he3hauka.toolvisuals.network.presence.*;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.CLIENT_LIST;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.CLIENT_LIST_SELF;

public final class ClientListClient {
    public enum Source {NEARBY,SYNCING,SERVER}
    private static final PresenceReplica REPLICA=new PresenceReplica();
    private static Object connection,world,player;
    private static UUID server;
    private static long epoch,lastRequest=Long.MIN_VALUE;
    private static boolean subscribed;
    private static Set<UUID> cachedPlayers;
    private static Map<UUID,String> cachedNames;
    private static UUID cachedSelf;
    private static boolean cachedIncludeSelf;
    private static List<PresenceRows.Entry> entries=List.of();

    private ClientListClient(){}
    public static void initialize(){
        PayloadTypeRegistry.serverboundPlay().register(PresencePayload.Request.TYPE,PresencePayload.Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PresencePayload.State.TYPE,PresencePayload.State.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(PresencePayload.State.TYPE,(payload,context)->{
            scope(context.client());
            if(wanted(context.client())&&PlaybackClient.available())REPLICA.accept(payload.value(),System.nanoTime());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->reset());
        ClientTickEvents.END_CLIENT_TICK.register(ClientListClient::tick);
    }

    private static void reset(){
        connection=world=player=null;server=null;epoch=0;lastRequest=Long.MIN_VALUE;subscribed=false;
        REPLICA.scope(null,0);cachedPlayers=null;cachedNames=null;cachedSelf=null;entries=List.of();
    }

    private static void scope(Minecraft client){
        if(connection!=client.getConnection()||world!=client.level||player!=client.player){
            reset();connection=client.getConnection();world=client.level;player=client.player;
        }
        if(!Objects.equals(server,PlaybackClient.server())||epoch!=PlaybackClient.epoch()){
            server=PlaybackClient.server();epoch=PlaybackClient.epoch();REPLICA.scope(server,epoch);
            subscribed=false;lastRequest=Long.MIN_VALUE;
        }
    }

    private static boolean supported(){return Minecraft.getInstance().getConnection()!=null&&ClientPlayNetworking.canSend(PresencePayload.Request.TYPE);}
    private static boolean wanted(Minecraft client){return ToolVisualsClient.CONFIG.active(CLIENT_LIST)||client.gui.screen() instanceof ru.he3hauka.toolvisuals.client.social.presence.ui.ClientBrowserScreen||client.gui.screen() instanceof ru.he3hauka.toolvisuals.client.social.profile.PlayerCardScreen;}
    public static Source source(){return supported()?(REPLICA.ready()&&PlaybackClient.available()?Source.SERVER:Source.SYNCING):Source.NEARBY;}

    private static void tick(Minecraft client){
        scope(client);
        if(client.player==null||client.level==null)return;
        long now=System.nanoTime();REPLICA.expire(now);
        boolean enabled=wanted(client);
        if(!enabled){
            if(subscribed&&supported())ClientPlayNetworking.send(new PresencePayload.Request(PresenceWire.Subscription.OFF));
            subscribed=false;lastRequest=Long.MIN_VALUE;REPLICA.scope(null,0);cachedPlayers=null;entries=List.of();return;
        }
        REPLICA.scope(server,epoch);
        if(!supported()||!PlaybackClient.available())return;
        long interval=REPLICA.ready()?30_000_000_000L:2_000_000_000L;
        if(lastRequest==Long.MIN_VALUE||now-lastRequest>=interval){
            ClientPlayNetworking.send(new PresencePayload.Request(REPLICA.ready()?PresenceWire.Subscription.KEEP:PresenceWire.Subscription.START));
            lastRequest=now;subscribed=true;
        }
    }

    public static List<PresenceRows.Entry> entries(){
        var client=Minecraft.getInstance();
        if(client.player==null||client.getConnection()==null)return List.of();
        scope(client);
        var advertised=source()==Source.SERVER?REPLICA.players():SocialClient.badges();
        var listed=PlayerRoster.listedOnline(client);
        boolean includeSelf=ToolVisualsClient.CONFIG.flag(CLIENT_LIST_SELF);UUID self=client.player.getUUID();
        if(cachedPlayers!=advertised||cachedNames!=listed||!Objects.equals(cachedSelf,self)||cachedIncludeSelf!=includeSelf){
            var next=PresenceRows.select(advertised,listed,self,includeSelf);if(!entries.equals(next))entries=next;
            cachedPlayers=advertised;cachedNames=listed;cachedSelf=self;cachedIncludeSelf=includeSelf;
        }
        return entries;
    }
}

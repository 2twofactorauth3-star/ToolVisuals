package ru.he3hauka.toolvisuals.client.animation;

import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.animation.network.PlaybackPayload;
import ru.he3hauka.toolvisuals.animation.protocol.PlaybackReplica;
import ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.player.ClientActions;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.CLIENT_BADGE;

public final class PlaybackClient {
    private static Object connection,world,player;
    private static PlaybackReplica replica;
    private static PlaybackWire.Hello hello;
    private static PlaybackWire.Request command;
    private static Boolean badge;
    private static long lastHello=Long.MIN_VALUE,lastCommand;
    private PlaybackClient(){}
    public static void initialize(){
        var c2s=net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.serverboundPlay();
        var s2c=net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay();
        c2s.register(PlaybackPayload.Request.TYPE,PlaybackPayload.Request.CODEC);
        s2c.register(PlaybackPayload.State.TYPE,PlaybackPayload.State.CODEC);
        s2c.register(PlaybackPayload.Reply.TYPE,PlaybackPayload.Reply.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(PlaybackPayload.State.TYPE,(payload,context)->{
            scope(context.client());
            if(replica!=null)apply(replica.state(payload.value(),System.nanoTime()));
        });
        ClientPlayNetworking.registerGlobalReceiver(PlaybackPayload.Reply.TYPE,(payload,context)->{
            scope(context.client());
            if(replica==null)return;
            apply(replica.reply(payload.value(),System.nanoTime()));
            if(hello!=null&&hello.requestId()==payload.value().requestId()&&replica.ready())hello=null;
            var reply=payload.value();
            if(command!=null&&command.requestId()==reply.requestId()&&reply.operation()==PlaybackWire.operation(command)
                    &&reply.state().actor().equals(replica.owner)&&reply.state().epoch()==replica.epoch()&&java.util.Objects.equals(reply.state().server(),replica.server()))command=null;
        });
    }
    public static void disconnect(){connection=world=player=null;replica=null;hello=null;command=null;badge=null;lastHello=Long.MIN_VALUE;}
    private static void scope(Minecraft client){
        if(connection!=client.getConnection()){
            disconnect();connection=client.getConnection();
            if(client.player!=null&&connection!=null)replica=new PlaybackReplica(client.player.getUUID());
        }
        if(client.player!=null&&connection!=null&&replica==null)replica=new PlaybackReplica(client.player.getUUID());
        if(world!=client.level||player!=client.player){
            world=client.level;player=client.player;
            if(replica!=null)replica.beginLife();
            hello=null;command=null;badge=null;lastHello=Long.MIN_VALUE;
        }
    }
    public static UUID server(){return replica==null?null:replica.server();}
    public static long epoch(){return replica==null?0:replica.epoch();}
    public static boolean available(){return replica!=null&&replica.ready()&&supported();}
    public static boolean supported(){return Minecraft.getInstance().getConnection()!=null&&ClientPlayNetworking.canSend(PlaybackPayload.Request.TYPE);}
    public static void tick(Minecraft client){
        scope(client);if(replica==null||client.player==null||client.level==null||!supported())return;
        long now=System.nanoTime();replica.expire(now);
        boolean current=ToolVisualsClient.CONFIG.active(CLIENT_BADGE);
        if(badge==null||badge!=current){badge=current;hello=replica.hello(current);lastHello=Long.MIN_VALUE;}
        long interval=replica.ready()?5_000_000_000L:500_000_000L;
        if(lastHello==Long.MIN_VALUE||now-lastHello>=interval){
            if(hello==null)hello=replica.hello(current);
            send(hello);lastHello=now;
        }
        if(command!=null&&now-lastCommand>=500_000_000L){send(command);lastCommand=now;}
    }
    public static void start(int emote,boolean moving,boolean synchronize){
        if(replica==null)return;
        if(!synchronize){sendCommand(replica.stop(System.nanoTime()));replica.local();return;}
        sendCommand(replica.start(emote,moving));
    }
    public static void stop(){stop(PlaybackReplica.StopReason.USER);}
    public static void stop(PlaybackReplica.StopReason reason){
        if(reason.revokeConsent)ru.he3hauka.toolvisuals.client.social.SocialClient.withdraw();
        if(replica!=null)sendCommand(replica.stop(System.nanoTime(),reason));
    }
    public static void unsynchronize(){stop(PlaybackReplica.StopReason.SYNC_DISABLED);if(replica!=null)replica.local();}
    public static void revokeConsent(){if(replica!=null)replica.revokeConsent();}
    public static void consent(UUID partner){if(replica!=null)replica.consent(partner,System.nanoTime());}
    public static void unload(UUID actor){if(replica!=null)replica.unload(actor);}
    private static void sendCommand(PlaybackWire.Request request){if(java.util.Objects.equals(command,request))return;command=request;if(request!=null){send(request);lastCommand=System.nanoTime();}}
    private static void send(PlaybackWire.Request request){if(supported())ClientPlayNetworking.send(new PlaybackPayload.Request(request));}
    private static void apply(PlaybackReplica.Change change){
        if(change==PlaybackReplica.Change.NONE)return;
        if(change.reset()){command=null;Emotes.resetPlayback();}
        else if(change.stopLocal()){command=null;Emotes.stop(false);}
        if(change.state()!=null&&change.state().pair()!=null&&(change.state().actor().equals(replica.owner)||change.state().pair().partner().equals(replica.owner))
                &&(!ToolVisualsClient.CONFIG.active(ru.he3hauka.toolvisuals.client.module.VisualModule.ANIMATIONS)||!ToolVisualsClient.CONFIG.flag(ru.he3hauka.toolvisuals.client.module.VisualSetting.EMOTE_SYNC))){stop();return;}
        if(change.state()!=null&&change.state().pair()!=null&&(change.state().actor().equals(replica.owner)||change.state().pair().partner().equals(replica.owner))){
            var local=change.state().actor().equals(replica.owner)?change.state():change.state().partnerState();
            ru.he3hauka.toolvisuals.client.social.SocialClient.started(local.pair().partner(),local.pair().id());
        }
        if(change.state()!=null&&!Emotes.receive(change.state())&&change.state().mode()!=PlaybackWire.Mode.STOPPED){
            replica.unload(change.state().actor());
            if(change.state().pair()!=null)replica.unload(change.state().pair().partner());
        }
        if(change.reason()!=null)ClientActions.notice("emotes.rejected."+change.reason().name().toLowerCase(java.util.Locale.ROOT));
    }
}

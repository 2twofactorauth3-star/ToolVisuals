package ru.he3hauka.toolvisuals.client.cosmetic.accessory;

import java.util.*;

import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.animation.EffectTime;
import ru.he3hauka.toolvisuals.client.animation.PlaybackClient;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation.*;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.network.AccessoryPayload;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.*;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.cosmetic.accessory.*;
import ru.he3hauka.toolvisuals.cosmetic.sync.CosmeticReplica;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.COSMETICS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class AccessoryClient {
    private record Publication(boolean receive,AccessoryLook look){}
    private static final CosmeticReplica<AccessoryWire.State> REMOTE=new CosmeticReplica<>(256);
    private static final CosmeticMotion MOTION=new CosmeticMotion();
    private static Object connection,player,world;
    private static UUID server;
    private static long epoch,sequence,lastSent;
    private static Publication publication;
    private static AccessoryWire.Request pending;
    private static boolean confirmed;
    private AccessoryClient(){}
    public static void initialize(){
        PayloadTypeRegistry.serverboundPlay().register(AccessoryPayload.Request.TYPE,AccessoryPayload.Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AccessoryPayload.State.TYPE,AccessoryPayload.State.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AccessoryPayload.ExpandedState.TYPE,AccessoryPayload.ExpandedState.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AccessoryPayload.Ack.TYPE,AccessoryPayload.Ack.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(AccessoryPayload.State.TYPE,(payload,context)->receive(context.client(),payload.value()));
        ClientPlayNetworking.registerGlobalReceiver(AccessoryPayload.ExpandedState.TYPE,(payload,context)->receive(context.client(),payload.value()));
        ClientPlayNetworking.registerGlobalReceiver(AccessoryPayload.Ack.TYPE,(payload,context)->{
            var ack=payload.value();
            if(pending!=null&&ack.requestId()==pending.requestId()&&ack.epoch()==epoch&&Objects.equals(server,ack.server())&&ack.accepted()){pending=null;confirmed=true;}
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->reset(true));
        ru.he3hauka.toolvisuals.client.replay.integration.ReplayLifecycle.unload((entity,level)->{REMOTE.unload(entity.getUUID());MOTION.forget(entity.getUUID());});
    }
    private static void receive(Minecraft client,AccessoryWire.State state){
        if(!available()||!Objects.equals(PlaybackClient.server(),state.server()))return;
        var result=REMOTE.accept(PlaybackClient.server(),state);if(!result.accepted())return;
        if(result.evicted()!=null)MOTION.forget(result.evicted());
        if(client.level==null||client.level.getPlayerByUUID(state.actor())==null)REMOTE.unload(state.actor());
    }
    private static void reset(boolean reconnect){
        REMOTE.clear();MOTION.clear();publication=null;pending=null;confirmed=false;server=null;epoch=0;
        if(reconnect){connection=player=world=null;sequence=0;}
    }
    public static boolean shared(){return available()&&confirmed&&pending==null;}
    public static AccessoryLook look(UUID actor){var client=Minecraft.getInstance();if(client.player==null)return null;if(actor.equals(client.player.getUUID()))return ToolVisualsClient.CONFIG.accessories();var state=REMOTE.state(actor);return state==null?null:state.design();}
    public static boolean available(){return PlaybackClient.available()&&ClientPlayNetworking.canSend(AccessoryPayload.Request.TYPE);}
    private static void scope(Minecraft client){
        if(connection!=client.getConnection()){reset(true);connection=client.getConnection();}
        if(player!=client.player||world!=client.level||epoch!=PlaybackClient.epoch()||!Objects.equals(server,PlaybackClient.server())){
            reset(false);player=client.player;world=client.level;epoch=PlaybackClient.epoch();server=PlaybackClient.server();
        }
    }
    public static void tick(Minecraft client){
        scope(client);
        if(client.player==null||!ToolVisualsClient.CONFIG.active(COSMETICS))MOTION.clear();else if(client.level!=null)MOTION.expire(client.level.getGameTime());
        if(client.player==null||!available()||epoch<=0||server==null)return;
        var config=ToolVisualsClient.CONFIG;boolean enabled=config.active(COSMETICS);
        var look=config.accessories();boolean publish=enabled&&config.flag(COSMETIC_SHARE)&&!look.empty();
        var next=new Publication(enabled&&config.flag(COSMETIC_OTHERS),publish?look:null);
        long now=System.nanoTime();
        if(!next.equals(publication)){
            if(publication!=null&&publication.receive!=next.receive)REMOTE.clear();
            publication=next;pending=new AccessoryWire.Request(++sequence,epoch,next.receive,next.look);confirmed=false;lastSent=Long.MIN_VALUE;
        }
        if(pending!=null&&(lastSent==Long.MIN_VALUE||now-lastSent>=2_000_000_000L)){ClientPlayNetworking.send(new AccessoryPayload.Request(pending));lastSent=now;}
    }
    public static AccessoryFrame frame(Entity actor,AvatarRenderState state,float partial){
        if(ru.he3hauka.toolvisuals.client.cosmetic.preview.PlayerPreview.preparing())return null;
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return null;
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;
        scope(client);
        if(client.player==null||client.level==null||actor.level()!=client.level||!config.active(COSMETICS)||state.isInvisible||state.isSpectator||NoRender.hidesEntity(actor))return null;
        boolean own=actor.getUUID().equals(client.player.getUUID());
        if(!config.flag(own?COSMETIC_SELF:COSMETIC_OTHERS))return null;
        double distance=actor.distanceToSqr(client.player),range=config.value(COSMETIC_DISTANCE);
        if(!own&&distance>range*range)return null;
        var remote=REMOTE.state(actor.getUUID());var look=own?config.accessories():remote==null?null:remote.design();
        if(look==null||look.empty())return null;
        boolean backVisible=!(config.flag(COSMETIC_HIDE_ELYTRA)&&state.chestEquipment.is(Items.ELYTRA));
        double time=EffectTime.at(client.level.getGameTime(),partial)/20;
        double seed=(actor.getUUID().getLeastSignificantBits()&1023)/1024d;
        double strength=config.flag(COSMETIC_MOTION)?config.value(COSMETIC_MOTION_STRENGTH):0;
        var motion=MOTION.sample(actor.getUUID(),client.player.getUUID(),client.level.getGameTime(),partial,look,CosmeticMovement.read(actor),strength,config.flag(COSMETIC_HEAD_SWAY));
        return new AccessoryFrame(look,time,seed,distance>24*24,backVisible,config.flag(COSMETIC_HIDE_CAPE),motion);
    }
}

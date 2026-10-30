package ru.he3hauka.toolvisuals.client.cape;

import java.util.*;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.PlayerSkin;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.animation.PlaybackClient;
import ru.he3hauka.toolvisuals.client.cape.network.CapePayload;
import ru.he3hauka.toolvisuals.cosmetic.cape.*;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.CAPES;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class CapeClient {
    private record Publication(boolean receive,CapeDesign design){}
    private static final ru.he3hauka.toolvisuals.cosmetic.sync.CosmeticReplica<CapeWire.State> REMOTE=new ru.he3hauka.toolvisuals.cosmetic.sync.CosmeticReplica<>(256);
    private static final Map<UUID,CapeTexture> TEXTURES=new HashMap<>();
    private static Object connection,player,world;
    private static UUID server;
    private static long epoch,sequence,lastSent;
    private static Publication publication;
    private static CapeWire.Request pending;
    private static boolean confirmed;
    private CapeClient(){}
    public static void initialize(){
        PayloadTypeRegistry.serverboundPlay().register(CapePayload.Request.TYPE,CapePayload.Request.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CapePayload.AnimatedRequest.TYPE,CapePayload.AnimatedRequest.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CapePayload.AnimatedState.TYPE,CapePayload.AnimatedState.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CapePayload.State.TYPE,CapePayload.State.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CapePayload.Ack.TYPE,CapePayload.Ack.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(CapePayload.State.TYPE,(payload,context)->receive(payload.value()));
        ClientPlayNetworking.registerGlobalReceiver(CapePayload.AnimatedState.TYPE,(payload,context)->receive(payload.value()));
        ClientPlayNetworking.registerGlobalReceiver(CapePayload.Ack.TYPE,(payload,context)->{
            var ack=payload.value();if(pending!=null&&ack.requestId()==pending.requestId()&&ack.epoch()==epoch&&Objects.equals(server,ack.server())&&ack.accepted()){pending=null;confirmed=true;}
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->client.execute(()->{
            if(connection==handler||connection==null&&client.getConnection()==null)reset(true);
        }));
        ru.he3hauka.toolvisuals.client.replay.integration.ReplayLifecycle.unload((entity,level)->{release(entity.getUUID());REMOTE.unload(entity.getUUID());});
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->reset(true));
    }
    private static void reset(boolean reconnect){
        TEXTURES.values().forEach(CapeTexture::close);TEXTURES.clear();REMOTE.clear();publication=null;pending=null;confirmed=false;server=null;epoch=0;
        if(reconnect){connection=null;player=null;world=null;sequence=0;}
    }
    public static boolean shared(){return available()&&confirmed&&pending==null;}
    public static boolean available(){return PlaybackClient.available()&&(ClientPlayNetworking.canSend(CapePayload.AnimatedRequest.TYPE)||ClientPlayNetworking.canSend(CapePayload.Request.TYPE));}
    public static boolean animatedSharing(){return ClientPlayNetworking.canSend(CapePayload.AnimatedRequest.TYPE);}
    public static void tick(Minecraft client){
        if(connection!=client.getConnection()){reset(true);connection=client.getConnection();}
        if(player!=client.player||world!=client.level||epoch!=PlaybackClient.epoch()||!Objects.equals(server,PlaybackClient.server())){
            reset(false);player=client.player;world=client.level;epoch=PlaybackClient.epoch();server=PlaybackClient.server();
        }
        if(client.player==null||!available()||epoch<=0||server==null)return;
        boolean enabled=ToolVisualsClient.CONFIG.active(CAPES);
        var selected=ToolVisualsClient.CAPES.selected();
        var next=new Publication(enabled&&ToolVisualsClient.CONFIG.flag(CAPE_OTHERS),enabled&&ToolVisualsClient.CONFIG.flag(CAPE_SHARE)&&selected!=null?selected.design():null);
        long now=System.nanoTime();
        if(!next.equals(publication)){
            if(publication!=null&&publication.receive!=next.receive){TEXTURES.values().forEach(CapeTexture::close);TEXTURES.clear();REMOTE.clear();}
            if(!enabled){TEXTURES.values().forEach(CapeTexture::close);TEXTURES.clear();}
            publication=next;pending=new CapeWire.Request(++sequence,epoch,next.receive,next.design);confirmed=false;lastSent=Long.MIN_VALUE;}
        if(pending!=null&&(lastSent==Long.MIN_VALUE||now-lastSent>=2_000_000_000L)){if(animatedSharing())ClientPlayNetworking.send(new CapePayload.AnimatedRequest(pending));else ClientPlayNetworking.send(new CapePayload.Request(pending));lastSent=now;}
    }
    private static void receive(CapeWire.State state){
        if(!available()||!Objects.equals(PlaybackClient.server(),state.server()))return;
        var result=REMOTE.accept(PlaybackClient.server(),state);if(!result.accepted())return;
        if(state.design()==null)release(state.actor());if(result.evicted()!=null)release(result.evicted());
        var client=Minecraft.getInstance();if(client.level==null||client.level.getPlayerByUUID(state.actor())==null)REMOTE.unload(state.actor());
    }

    private static void release(UUID actor){var texture=TEXTURES.remove(actor);if(texture!=null)texture.close();}
    public static CapeDesign design(UUID actor){
        var client=Minecraft.getInstance();if(client.player==null)return null;
        if(actor.equals(client.player.getUUID())){var selected=ToolVisualsClient.CAPES.selected();return selected==null?null:selected.design();}
        var remote=REMOTE.state(actor);return remote==null?null:remote.design();
    }
    public static void apply(UUID actor,AvatarRenderState state){
        if(ru.he3hauka.toolvisuals.client.cosmetic.preview.PlayerPreview.preparing())return;
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;
        var client=Minecraft.getInstance();
        if(client.player==null||!ToolVisualsClient.CONFIG.active(CAPES))return;
        boolean own=actor.equals(client.player.getUUID());
        if(!ToolVisualsClient.CONFIG.flag(own?CAPE_SELF:CAPE_OTHERS)){state.showCape=false;return;}
        CapeDesign design=design(actor);
        if(design==null)return;
        if(!ToolVisualsClient.CONFIG.flag(CAPE_ANIMATIONS))design=design.frame(0);
        if(!TEXTURES.containsKey(actor)&&TEXTURES.size()>=257)return;
        var texture=TEXTURES.computeIfAbsent(actor,id->new CapeTexture());
        texture.apply(state,design);
    }
    public static void skin(AvatarRenderState state,net.minecraft.core.ClientAsset.Texture cape){
        if(state.skin==null||cape==null)return;
        var old=state.skin;var elytra=old.elytra()!=null?old.elytra():old.cape()!=null&&!custom(old.cape().texturePath())?old.cape():null;state.skin=new PlayerSkin(old.body(),cape,elytra,old.model(),old.secure());state.showCape=true;
    }
    public static boolean custom(net.minecraft.resources.Identifier id){return id.getNamespace().equals("toolvisuals")&&id.getPath().startsWith("capes/");}
}

package ru.he3hauka.toolvisuals.client.friends.network;

import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.animation.PlaybackClient;
import ru.he3hauka.toolvisuals.client.friends.*;
import ru.he3hauka.toolvisuals.network.friends.FriendOutbox;
import ru.he3hauka.toolvisuals.network.friends.FriendWire;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.FRIENDS_LIST;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class FriendDataClient {
    private static final Map<UUID,FriendData> SHARED=new HashMap<>();
    private static final Map<UUID,Long> SEQUENCES=new HashMap<>();
    private static Map<String,FriendData> visible=Map.of();
    private static Map<UUID,Integer> desired=Map.of();
    private static Object connection,world;
    private static UUID server,hover;
    private static long epoch,sequence,revision,lastSample;
    private static FriendOutbox outbox;
    private FriendDataClient(){}
    public static boolean enabled(){return ToolVisualsClient.CONFIG.active(FRIENDS_LIST)&&Friends.available();}
    public static long revision(){return revision;}
    public static FriendData data(String name){return visible.getOrDefault(name.toLowerCase(Locale.ROOT),FriendData.UNKNOWN);}
    public static void hover(String name){hover=null;if(name!=null)for(var entry:Friends.onlinePlayers().entrySet())if(entry.getValue().equalsIgnoreCase(name)){hover=entry.getKey();break;}}
    public static void initialize(){
        PayloadTypeRegistry.serverboundPlay().register(FriendPayload.Request.TYPE,FriendPayload.Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FriendPayload.State.TYPE,FriendPayload.State.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(FriendPayload.State.TYPE,(payload,context)->receive(payload.value()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{if(connection==handler)reset(true);});
        ClientTickEvents.END_CLIENT_TICK.register(FriendDataClient::tick);
    }
    private static void reset(boolean disconnected){SHARED.clear();SEQUENCES.clear();visible=Map.of();desired=Map.of();outbox=null;hover=null;revision++;lastSample=0;if(disconnected){connection=null;world=null;server=null;epoch=sequence=0;}}
    private static void restart(){SHARED.clear();SEQUENCES.clear();visible=Map.of();revision++;outbox=new FriendOutbox(()->++sequence);}
    private static void tick(Minecraft client){
        if(connection!=client.getConnection()){reset(true);connection=client.getConnection();}
        if(world!=client.level||epoch!=PlaybackClient.epoch()||!Objects.equals(server,PlaybackClient.server())){reset(false);world=client.level;epoch=PlaybackClient.epoch();server=PlaybackClient.server();}
        boolean active=enabled()&&client.player!=null&&client.level!=null;
        long now=System.nanoTime();
        if(active&&now-lastSample>=250_000_000L){sample(client);lastSample=now;}
        if(!active){if(!visible.isEmpty()||!SHARED.isEmpty()){SHARED.clear();SEQUENCES.clear();visible=Map.of();revision++;}desired=Map.of();hover=null;if(outbox!=null)outbox.stop();}
        if(client.getConnection()==null||!PlaybackClient.available()||server==null||!ClientPlayNetworking.canSend(FriendPayload.Request.TYPE))return;
        if(outbox!=null&&outbox.closed()){if(active)restart();else outbox=null;}
        if(active&&outbox==null)restart();
        if(outbox==null)return;
        if(outbox.acknowledged()>0&&now-outbox.acknowledged()>12_000_000_000L&&!SHARED.isEmpty()){SHARED.clear();SEQUENCES.clear();revision++;lastSample=0;}
        var request=outbox.next(desired,now);if(request!=null)ClientPlayNetworking.send(new FriendPayload.Request(request));
    }
    private static void sample(Minecraft client){
        var config=ToolVisualsClient.CONFIG;var requested=new LinkedHashMap<UUID,Integer>();var rows=new LinkedHashMap<String,FriendData>();
        boolean cursor=client.gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen;
        for(var online:Friends.onlinePlayers().entrySet()){
            var friend=ToolVisualsClient.FRIENDS.get(online.getValue());if(friend==null)continue;
            if(rows.containsKey(friend.name.toLowerCase(Locale.ROOT)))continue;
            if(requested.size()>=FriendWire.MAX_FRIENDS)break;
            int fields=0;if(config.flag(FRIENDS_LIST_HEALTH)&&friend.display.health)fields|=FriendWire.HEALTH;if(config.flag(FRIENDS_LIST_ARMOR)&&friend.display.armor)fields|=FriendWire.ARMOR;
            if(config.flag(FRIENDS_LIST_INVENTORY)&&friend.display.inventory&&(friend.display.mode((int)config.value(FRIENDS_LIST_INVENTORY_MODE))==0||cursor&&online.getKey().equals(hover)))fields|=FriendWire.INVENTORY;
            requested.put(online.getKey(),fields);var synced=SHARED.getOrDefault(online.getKey(),FriendData.UNKNOWN);
            Player entity=client.level.getPlayerByUUID(online.getKey());boolean known=entity!=null&&!entity.isRemoved()&&Friends.renderAllowed(entity);
            boolean health=synced.healthKnown||known;
            float hp=synced.healthKnown?synced.health:known?entity.getHealth():0,max=synced.healthKnown?synced.maximum:known?entity.getMaxHealth():0,abs=synced.healthKnown?synced.absorption:known?entity.getAbsorptionAmount():0;
            List<ItemStack> armor=synced.armor;
            if(armor.isEmpty()&&known)armor=List.of(entity.getItemBySlot(EquipmentSlot.HEAD).copy(),entity.getItemBySlot(EquipmentSlot.CHEST).copy(),entity.getItemBySlot(EquipmentSlot.LEGS).copy(),entity.getItemBySlot(EquipmentSlot.FEET).copy());
            rows.put(friend.name.toLowerCase(Locale.ROOT),new FriendData(health,hp,max,abs,armor,synced.inventory));
        }
        desired=Collections.unmodifiableMap(requested);visible=Collections.unmodifiableMap(rows);SHARED.keySet().retainAll(requested.keySet());SEQUENCES.keySet().retainAll(requested.keySet());revision++;
    }
    private static void receive(FriendWire.State state){
        if(outbox==null||!Objects.equals(server,state.server())||!outbox.session.equals(state.session()))return;
        if(state.kind()==FriendWire.Kind.REPLY){outbox.acknowledge(state,System.nanoTime());return;}
        if(!enabled()||!desired.containsKey(state.actor())||state.sequence()<=SEQUENCES.getOrDefault(state.actor(),0L))return;
        SEQUENCES.put(state.actor(),state.sequence());
        if(state.kind()==FriendWire.Kind.CLEAR)SHARED.remove(state.actor());
        else{
            var old=SHARED.getOrDefault(state.actor(),FriendData.UNKNOWN);
            if(state.kind()==FriendWire.Kind.STATUS)SHARED.put(state.actor(),new FriendData((state.fields()&FriendWire.HEALTH)!=0,state.health(),state.maximum(),state.absorption(),FriendItems.decode(state.items()),old.inventory));
            else SHARED.put(state.actor(),new FriendData(old.healthKnown,old.health,old.maximum,old.absorption,old.armor,FriendItems.decode(state.items())));
        }
        lastSample=0;revision++;
    }
}

package ru.he3hauka.toolvisuals.client.potion.tracker;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.player.ClientActions;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.BUFF_TRACKER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class BuffTracker {
    private static final class Drinking {
        final ItemStack item;
        final net.minecraft.world.InteractionHand hand;
        int remaining;
        long stopped=-1;
        Drinking(Player player){item=player.getUseItem().copy();hand=player.getUsedItemHand();remaining=player.getUseItemRemainingTicks();}
    }
    private static final BuffTimeline TIMELINE=new BuffTimeline();
    private static final Map<UUID,Drinking> DRINKS=new HashMap<>();
    private static final Map<String,Long> OWN_EFFECTS=new HashMap<>();
    private record PendingDrink(ItemStack item,long due){}
    private static final List<PendingDrink> PENDING=new ArrayList<>();
    private static List<BuffTimeline.Entry> visible=List.of();
    private static Object connection,world,owner;
    private static long ticks,revision;
    private BuffTracker(){}
    public static long revision(){return revision;}
    public static List<BuffTimeline.Entry> entries(){return visible;}
    private static boolean enabled(){return ToolVisualsClient.CONFIG.active(BUFF_TRACKER)&&!ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active();}
    private static boolean allowed(Player player){var config=ToolVisualsClient.CONFIG;var client=Minecraft.getInstance();return config.flag(player==client.player?BUFF_SELF:BUFF_OTHERS)&&player.isAlive()&&client.player!=null&&client.player.distanceToSqr(player)<=Math.pow(config.value(BUFF_RANGE),2)&&(!ru.he3hauka.toolvisuals.client.connection.holyworld.HolyWorldClient.protectedServer()||!player.isInvisible());}
    private static void reset(Minecraft client){DRINKS.clear();OWN_EFFECTS.clear();PENDING.clear();TIMELINE.reset(client.player==null?null:client.player.getUUID());visible=List.of();ticks=0;revision++;}
    private static void scope(Minecraft client){if(connection!=client.getConnection()||world!=client.level||owner!=client.player){connection=client.getConnection();world=client.level;owner=client.player;reset(client);}}
    public static void tick(Minecraft client){
        scope(client);
        if(!enabled()||client.level==null||client.player==null){if(!DRINKS.isEmpty()||!visible.isEmpty()||!OWN_EFFECTS.isEmpty()||!PENDING.isEmpty())reset(client);return;}if(client.isPaused())return;ticks++;
        var completed=PENDING.iterator();while(completed.hasNext()){var next=completed.next();if(ticks>=next.due){completed.remove();if(allowed(client.player))applyConsumed(client.player,next.item,false);}}
        var seen=new HashSet<UUID>();
        for(var player:client.level.players()){
            UUID id=player.getUUID();if(!player.isAlive()){TIMELINE.remove(id);DRINKS.remove(id);if(player==client.player)OWN_EFFECTS.clear();continue;}if(!allowed(player))continue;seen.add(id);
            var prior=DRINKS.get(id);boolean using=player.isUsingItem()&&(player.getUseItem().is(Items.POTION)||player.getUseItem().is(Items.MILK_BUCKET));
            if(using){
                if(prior==null||!ItemStack.isSameItemSameComponents(prior.item,player.getUseItem())||prior.hand!=player.getUsedItemHand()){
                    if(DRINKS.size()<128){prior=new Drinking(player);DRINKS.put(id,prior);}
                }
                if(prior!=null){prior.remaining=Math.min(prior.remaining,player.getUseItemRemainingTicks());prior.stopped=-1;}
            }else if(prior!=null){
                if(prior.stopped<0)prior.stopped=ticks;
                var current=player.getItemInHand(prior.hand);boolean changed=current.is(prior.item.is(Items.MILK_BUCKET)?Items.BUCKET:Items.GLASS_BOTTLE)||ItemStack.isSameItemSameComponents(prior.item,current)&&current.getCount()<prior.item.getCount();
                if(prior.remaining<=4&&changed){DRINKS.remove(id);consumed(player,prior.item,true);}
                else if(ticks-prior.stopped>5)DRINKS.remove(id);
            }
        }
        DRINKS.keySet().retainAll(seen);
        if(ticks%5==0){
            var settings=ToolVisualsClient.CONFIG;
            var previous=TIMELINE.entries(ticks);for(var entry:previous){
                if(!settings.flag(entry.player().equals(client.player.getUUID())?BUFF_SELF:BUFF_OTHERS)){TIMELINE.remove(entry.player());continue;}
                if(!settings.flag(BUFF_NEGATIVE))for(var tracked:entry.effects()){var type=effect(tracked.id());if(type!=null&&!type.value().isBeneficial())TIMELINE.remove(entry.player(),tracked.id());}
            }
            if(!settings.flag(BUFF_SELF))OWN_EFFECTS.clear();
            var observed=OWN_EFFECTS.entrySet().iterator();while(observed.hasNext()){
                var tracked=observed.next();var type=effect(tracked.getKey());var active=type==null?null:client.player.getEffect(type);
                if(active==null){if(ticks-tracked.getValue()>20){TIMELINE.remove(client.player.getUUID(),tracked.getKey());observed.remove();}}
                else if(settings.flag(BUFF_NEGATIVE)||active.getEffect().value().isBeneficial())TIMELINE.confirmed(client.player.getUUID(),client.player.getName().getString(),tracked.getKey(),active.getAmplifier(),active.isInfiniteDuration()?-1:active.getDuration(),ticks);
            }
            visible=TIMELINE.entries(ticks);revision++;
        }
    }
    public static void event(ClientboundEntityEventPacket packet){
        var client=Minecraft.getInstance();scope(client);if(!enabled()||client.level==null||!(packet.getEntity(client.level) instanceof Player player))return;
        if(packet.getEventId()==3){TIMELINE.remove(player.getUUID());DRINKS.remove(player.getUUID());return;}
        if(packet.getEventId()!=9||!allowed(player))return;var prior=DRINKS.remove(player.getUUID());var item=prior==null?player.getUseItem():prior.item;if(item.is(Items.POTION)||item.is(Items.MILK_BUCKET))consumed(player,item,player!=client.player);
    }
    private static void consumed(Player player,ItemStack item,boolean estimated){
        if(player==Minecraft.getInstance().player){if(PENDING.size()<8)PENDING.add(new PendingDrink(item.copy(),ticks+2));return;}
        applyConsumed(player,item,estimated);
    }
    private static void applyConsumed(Player player,ItemStack item,boolean estimated){
        if(item.is(Items.MILK_BUCKET)){TIMELINE.remove(player.getUUID());if(player==Minecraft.getInstance().player)OWN_EFFECTS.clear();visible=TIMELINE.entries(ticks);revision++;return;}
        var contents=item.get(DataComponents.POTION_CONTENTS);var names=Component.empty();int count=0;
        if(contents!=null)for(var effect:contents.getAllEffects()){
            if(count>=32)break;if(!ToolVisualsClient.CONFIG.flag(BUFF_NEGATIVE)&&!effect.getEffect().value().isBeneficial())continue;
            String id=BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();int duration=effect.isInfiniteDuration()?-1:(int)Math.clamp(Math.round(effect.getDuration()*PotionScale.value(item)),1,Integer.MAX_VALUE);
            var current=player==Minecraft.getInstance().player?player.getEffect(effect.getEffect()):null;
            boolean instant=PotionScale.instant(effect.getEffect().value());
            if(!instant&&player==Minecraft.getInstance().player&&OWN_EFFECTS.size()<32)OWN_EFFECTS.put(id,ticks);
            if(!instant){if(current!=null)TIMELINE.confirmed(player.getUUID(),player.getName().getString(),id,current.getAmplifier(),current.isInfiniteDuration()?-1:current.getDuration(),ticks);
            else TIMELINE.add(player.getUUID(),player.getName().getString(),id,effect.getAmplifier(),duration,true,ticks);}
            if(count++>0)names.append(Component.literal(", ").withColor(0x8e96ab));
            names.append(effect.getEffect().value().getDisplayName().copy().withColor(effect.getEffect().value().getColor()));
            if(effect.getAmplifier()>0)names.append(Component.literal(" "+(effect.getAmplifier()+1)).withColor(0xd9cef8));
            String clock=instant?UiI18n.tr("toolvisuals.buffs.instant").getString():current==null?time(duration,true):UiI18n.tr("toolvisuals.buffs.duration_remaining",time(duration,true),time(current.isInfiniteDuration()?-1:current.getDuration(),false)).getString();
            names.append(Component.literal(" "+clock).withColor(0xa8b3cb));
        }
        if(ToolVisualsClient.CONFIG.flag(BUFF_NOTIFY)&&(count>0||contents==null||!contents.hasEffects()))ClientActions.notice(estimated?"buffs.observed":"buffs.consumed",player.getName(),count==0?UiI18n.tr("toolvisuals.buffs.unknown"):names);
        visible=TIMELINE.entries(ticks);revision++;
    }
    public static Holder<MobEffect> effect(String id){var key=Identifier.tryParse(id);return key==null?null:BuiltInRegistries.MOB_EFFECT.get(key).orElse(null);}
    public static String time(long remaining,boolean estimated){return (estimated?"~":"")+(remaining<0?"∞":HudFormats.duration((int)Math.min(Integer.MAX_VALUE,remaining),false));}
}

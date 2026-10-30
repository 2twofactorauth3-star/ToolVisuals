package ru.he3hauka.toolvisuals.client.hud.cooldown;

import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;

public final class Cooldowns {
    private static final CooldownTimeline TIMELINE=new CooldownTimeline();
    private static ItemCooldowns owner;
    private static final LinkedHashMap<Identifier,ItemStack> ITEMS=new LinkedHashMap<>();
    private Cooldowns() {}
    private static boolean own(ItemCooldowns source) {
        var player=Minecraft.getInstance().player;
        if(player==null||player.getCooldowns()!=source)return false;
        if(owner!=source){owner=source;TIMELINE.clear();ITEMS.clear();}
        return true;
    }
    public static void remember(ItemCooldowns source,ItemStack stack) {
        if(stack.isEmpty()||!own(source))return;
        var group=source.getCooldownGroup(stack);
        if(!ITEMS.containsKey(group)&&ITEMS.size()>=256)ITEMS.remove(ITEMS.keySet().iterator().next());
        ITEMS.put(group,stack.copyWithCount(1));
    }
    public static void started(ItemCooldowns source,Identifier group,int duration){if(own(source)){TIMELINE.start(group.toString(),duration);if(duration>0)item(group);}}
    public static void ended(ItemCooldowns source,Identifier group){if(own(source))TIMELINE.start(group.toString(),0);}
    public static void tick(ItemCooldowns source){if(own(source))TIMELINE.tick();}
    public static List<CooldownTimeline.Entry> entries(float partial) {
        var player=Minecraft.getInstance().player;
        return player==null||player.getCooldowns()!=owner?List.of():TIMELINE.entries(partial);
    }
    public static ItemStack item(Identifier group) {
        var player=Minecraft.getInstance().player;
        if(player==null||!own(player.getCooldowns()))return ItemStack.EMPTY;
        var remembered=ITEMS.get(group);if(remembered!=null)return remembered;
        for(int slot=0;slot<41;slot++) {
            var stack=player.getInventory().getItem(slot);
            if(!stack.isEmpty()&&player.getCooldowns().getCooldownGroup(stack).equals(group)) {
                remember(player.getCooldowns(),stack);return ITEMS.get(group);
            }
        }
        var stack=BuiltInRegistries.ITEM.getOptional(group).map(ItemStack::new).orElse(ItemStack.EMPTY);
        if(!stack.isEmpty()){remembered=stack.copyWithCount(1);if(ITEMS.size()>=256)ITEMS.remove(ITEMS.keySet().iterator().next());ITEMS.put(group,remembered);return remembered;}
        return ItemStack.EMPTY;
    }
}

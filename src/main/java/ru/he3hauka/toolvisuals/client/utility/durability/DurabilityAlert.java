package ru.he3hauka.toolvisuals.client.utility.durability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class DurabilityAlert {
    private static final DurabilityWarnings WARNINGS=new DurabilityWarnings();
    private static final SystemToast.SystemToastId TOAST=new SystemToast.SystemToastId(5000);
    private static Object player;
    private static int ticks;
    private DurabilityAlert(){}
    public static void tick(Minecraft client) {
        if(player!=client.player||!ToolVisualsClient.CONFIG.active(DURABILITY_ALERT)){player=client.player;WARNINGS.clear();ticks=0;}
        if(client.player==null||!ToolVisualsClient.CONFIG.active(DURABILITY_ALERT)||++ticks%10!=0)return;
        var config=ToolVisualsClient.CONFIG;
        for(var slot:EquipmentSlot.values()) {
            boolean hand=slot==EquipmentSlot.MAINHAND||slot==EquipmentSlot.OFFHAND;
            if(hand?!config.flag(ALERT_HANDS):!config.flag(ALERT_ARMOR))continue;
            ItemStack item=client.player.getItemBySlot(slot);
            if(WARNINGS.shouldWarn(slot.name(),BuiltInRegistries.ITEM.getKey(item.getItem()).toString(),item.getDamageValue(),item.getMaxDamage(),(int)config.value(ALERT_THRESHOLD),client.player.tickCount,(long)config.value(ALERT_REPEAT)*20)) {
                int remaining=Math.max(0,item.getMaxDamage()-item.getDamageValue());
                SystemToast.addOrUpdate(client.gui.toastManager(),TOAST,UiI18n.tr("toolvisuals.utility.durability_warning"),UiI18n.tr("toolvisuals.utility.durability_remaining",item.getHoverName(),remaining));
                if(config.flag(ALERT_SOUND))client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP,.6f,.75f);
            }
        }
    }
}

package ru.he3hauka.toolvisuals.client.utility.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ItemInfo {
    private ItemInfo(){}
    public static void tooltip(ItemStack item,Item.TooltipContext context,TooltipFlag flag,List<Component> lines) {
        var config=ToolVisualsClient.CONFIG;if(!config.active(ITEM_INFO))return;
        if(config.flag(ITEM_INFO_DURABILITY)&&item.getMaxDamage()>0)lines.add(UiI18n.tr("toolvisuals.utility.item_durability",item.getMaxDamage()-item.getDamageValue(),item.getMaxDamage(),Math.round(100f*(item.getMaxDamage()-item.getDamageValue())/item.getMaxDamage())).withStyle(ChatFormatting.GRAY));
        var container=item.get(DataComponents.CONTAINER);
        if(config.flag(ITEM_INFO_CONTAINER)&&container!=null)lines.add(UiI18n.tr("toolvisuals.utility.item_container",container.nonEmptyItemCopyStream().count(),Math.max(27,container.size())).withStyle(ChatFormatting.GRAY));
        var food=item.get(DataComponents.FOOD);
        if(config.flag(ITEM_INFO_FOOD)&&food!=null)lines.add(UiI18n.tr("toolvisuals.utility.item_food",food.nutrition(),String.format(java.util.Locale.ROOT,"%.1f",food.saturation())).withStyle(ChatFormatting.GRAY));
    }
}

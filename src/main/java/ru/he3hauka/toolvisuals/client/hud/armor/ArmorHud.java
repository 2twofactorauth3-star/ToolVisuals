package ru.he3hauka.toolvisuals.client.hud.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ArmorHud {
    public static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final VisualSetting[] GROUPS = {ARMOR_HELMET_GROUP, ARMOR_CHEST_GROUP, ARMOR_LEGS_GROUP, ARMOR_BOOTS_GROUP};
    private static final HudElement[] ELEMENTS = {HudElement.ARMOR, HudElement.ARMOR_2, HudElement.ARMOR_3, HudElement.ARMOR_4};
    private static final ItemStack[] PREVIEW = {new ItemStack(Items.DIAMOND_HELMET), new ItemStack(Items.DIAMOND_CHESTPLATE),
            new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS)};
    private ArmorHud() {}
    public static ItemStack preview(int slot) { return PREVIEW[slot]; }
    public static float remaining(ItemStack item) {
        return item.getMaxDamage() > 0 ? Math.clamp(1 - item.getDamageValue() / (float)item.getMaxDamage(), 0, 1) : 1;
    }
    public static boolean showsDurability(ItemStack item, boolean bars, boolean fullBar) {
        return bars && !item.isEmpty() && item.getMaxDamage() > 0 && (item.getDamageValue() > 0 || fullBar);
    }
    public static void bar(GuiGraphicsExtractor g, float x, float y, float w, float fraction, int color, UiPalette p) {
        UiPaint.rounded(g, x, y, w, 2, 1, p.track);
        if (fraction > 0) UiPaint.rounded(g, x, y, w * fraction, 2, Math.min(1, w * fraction / 2), color);
    }
    public static void draw(GuiGraphicsExtractor g, UiPalette p) {
        var client = Minecraft.getInstance(); var config = ToolVisualsClient.CONFIG;
        for (int group = 0; group < 4; group++) {
            int count = 0; boolean bars = false;
            for (int i = 0; i < 4; i++) if ((int)config.value(GROUPS[i]) == group) {
                var item = HudPlacement.preview() ? PREVIEW[i] : client.player.getItemBySlot(SLOTS[i]);
                if (!item.isEmpty()) {
                    count++; bars |= showsDurability(item, config.flag(ARMOR_DURABILITY), config.flag(ARMOR_FULL_BAR));
                }
            }
            if (count == 0) continue;
            int width = count * 20 + 4, height = bars ? 25 : 22;
            int x = g.guiWidth() / 2 - width / 2, y = g.guiHeight() - 76 - group * 30;
            HudPlacement.place(g, ELEMENTS[group], x, y, width, height);
            UiPaint.rounded(g, x, y, width, height, 5, p.hudPanel);
            int col = 0;
            for (int i = 0; i < 4; i++) if ((int)config.value(GROUPS[i]) == group) {
                var item = HudPlacement.preview() ? PREVIEW[i] : client.player.getItemBySlot(SLOTS[i]);
                if (item.isEmpty()) continue;
                int sx = x + 4 + col++ * 20;
                SmoothHudItems.item(g, item, sx, y + 3);
                if (showsDurability(item, config.flag(ARMOR_DURABILITY), config.flag(ARMOR_FULL_BAR)))
                    bar(g, sx, y + 21, 16, remaining(item), UiPalette.mix(0xffff7184, 0xff7cebc5, remaining(item)), p);
            }
            g.pose().popMatrix();
        }
    }
}

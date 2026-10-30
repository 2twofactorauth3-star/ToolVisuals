package ru.he3hauka.toolvisuals.client.hud.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.INVENTORY_HUD;

public final class InventoryHud {
    private static final class Samples {
        static final net.minecraft.world.item.ItemStack PEARLS=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_PEARL,16);
        static final net.minecraft.world.item.ItemStack APPLES=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE,4);
    }
    private InventoryHud() {}
    public static int slot(int row, int column) { return row < 3 ? 9 + row * 9 + column : column; }
    public static void draw(GuiGraphicsExtractor g, UiPalette palette) {
        var client = Minecraft.getInstance(); var config = ToolVisualsClient.CONFIG;
        boolean hotbar = config.flag(INVENTORY_HOTBAR);
        var inventory = client.player.getInventory();
        if(!HudPlacement.preview()&&config.flag(INVENTORY_HIDE_EMPTY)){
            boolean occupied=false;
            for(int index=hotbar?0:9;index<36;index++)if(!inventory.getItem(index).isEmpty()){occupied=true;break;}
            ModuleLog.state(INVENTORY_HUD,"visibility",occupied?"visible_items":"hidden_empty");
            if(!occupied)return;
        }else ModuleLog.state(INVENTORY_HUD,"visibility",HudPlacement.preview()?"editor_preview":"visible_hide_empty_disabled");
        boolean title = !config.flag(INVENTORY_HIDE_TITLE);
        float density = Math.clamp(client.getWindow().getHeight() / 720f, 1, 2);
        float scale = density * config.number(INVENTORY_SCALE) / client.getWindow().getGuiScale();
        g.pose().pushMatrix(); g.pose().scale(scale, scale);
        int rows = hotbar ? 4 : 3;
        float header = title ? 18 : 3;
        float width = 168, height = header + rows * 18 - 1 + 3 + (hotbar ? 2 : 0);
        float x = HudFormats.position(g.guiWidth() / scale, width, HudPlacement.value(INVENTORY_X));
        float y = HudFormats.position(g.guiHeight() / scale, height, HudPlacement.value(INVENTORY_Y));
        HudPlacement.place(g, HudElement.INVENTORY, x, y, width, height);
        UiPaint.rounded(g, x, y, width, height, 6, palette.hudPanel);
        if (title) UiPaint.baseline(g, UiI18n.tr("toolvisuals.hud.inventory").getString(), x + 6, UiPaint.textBaseline(g,y,18,.75f), .75f, palette.accentText, 1);
        for (int row = 0; row < rows; row++) for (int col = 0; col < 9; col++) {
            int slot = slot(row, col);
            int sx = Math.round(x + 3 + col * 18), sy = Math.round(y + header + row * 18 + (row == 3 ? 2 : 0));
            UiPaint.rounded(g, sx, sy, 17, 17, 2, row == 3 && col == inventory.getSelectedSlot() ? palette.hover : UiPaint.alpha(palette.raised, .6f));
            var item = HudPlacement.samples()?(slot%5==0?slot%2==0?Samples.PEARLS:Samples.APPLES:net.minecraft.world.item.ItemStack.EMPTY):inventory.getItem(slot);
            if (!item.isEmpty()) { SmoothHudItems.item(g, item, sx + 1, sy + 1); SmoothHudItems.decorations(g, item, sx + 1, sy + 1, palette); }
        }
        g.pose().popMatrix(); g.pose().popMatrix();
    }
}

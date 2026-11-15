package ru.he3hauka.toolvisuals.client.items.highlight;

import java.util.IdentityHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.items.highlight.ui.ItemHighlightsScreen;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_HIGHLIGHTS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ItemHighlights {
    public record GuiStyle(int color, float radius) {}
    public interface GuiState { GuiStyle toolvisuals$itemGlow(); }
    private static final GuiStyle NONE = new GuiStyle(0, 0);
    private static final ThreadLocal<GuiStyle> GUI = ThreadLocal.withInitial(() -> NONE);
    private static final ThreadLocal<Boolean> HOTBAR = ThreadLocal.withInitial(() -> false);
    private static final IdentityHashMap<Item, Integer> COLORS = new IdentityHashMap<>();
    private static ItemHighlightRules previous;
    private ItemHighlights() {}

    public static int color(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var rules = ToolVisualsClient.CONFIG.itemHighlights();
        if (rules != previous) {
            COLORS.clear();
            for (Item item : BuiltInRegistries.ITEM) {
                Integer color = rules.colors.get(BuiltInRegistries.ITEM.getKey(item).toString());
                if (color != null) COLORS.put(item, 0xff000000 | color);
            }
            previous = rules;
        }
        return COLORS.getOrDefault(stack.getItem(), 0);
    }
    public static GuiStyle capture(ItemStack stack) {
        var before = GUI.get(); var config = ToolVisualsClient.CONFIG;
        var screen = Minecraft.getInstance().gui.screen();
        boolean preview = screen instanceof ItemHighlightsScreen && config.available(ITEM_HIGHLIGHTS);
        boolean visible = preview || config.active(ITEM_HIGHLIGHTS) && (HOTBAR.get() ? config.flag(ITEM_HIGHLIGHT_HOTBAR) : screen instanceof AbstractContainerScreen<?> && config.flag(ITEM_HIGHLIGHT_INVENTORY));
        int color = visible ? color(stack) : 0;
        GUI.set(color == 0 ? NONE : new GuiStyle((color & 0xffffff) | Math.round(config.number(ITEM_HIGHLIGHT_STRENGTH) * 255) << 24, config.number(ITEM_HIGHLIGHT_RADIUS)));
        return before;
    }
    public static GuiStyle extracting() { return GUI.get(); }
    public static void restore(GuiStyle style) { GUI.set(style); }
    public static boolean hotbar() { boolean previous = HOTBAR.get(); HOTBAR.set(true); return previous; }
    public static void restoreHotbar(boolean previous) { HOTBAR.set(previous); }
}

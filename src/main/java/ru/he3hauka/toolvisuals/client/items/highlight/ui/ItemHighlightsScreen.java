package ru.he3hauka.toolvisuals.client.items.highlight.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlightRules;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

public final class ItemHighlightsScreen extends VisualEditorScreen {
    private record Entry(String id, ItemStack stack, String name, String search, boolean available) {}
    private static final int ROWS = 6;
    private final List<Entry> registry = new ArrayList<>();
    private List<Entry> matches = List.of();
    private String query = "", selected = "minecraft:totem_of_undying";
    private boolean adding;
    private int page;
    private EditBox search;
    private Entry chosen;

    public ItemHighlightsScreen(Screen parent) { super(parent, text("title")); }
    private static Component text(String key, Object... args) { return UiI18n.tr("toolvisuals.item_highlights." + key, args); }
    private static ClientConfig config() { return ToolVisualsClient.CONFIG; }
    @Override protected void init() {
        frame(760, 548);
        registry.clear();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            var stack = new ItemStack(item); String name = stack.getHoverName().getString();
            registry.add(new Entry(id, stack, name, (name + " " + id).toLowerCase(Locale.ROOT), true));
        }
        var known = registry.stream().map(Entry::id).collect(Collectors.toSet());
        for (String id : config().itemHighlights().colors.keySet()) if (!known.contains(id))
            registry.add(new Entry(id, new ItemStack(Items.BARRIER), text("unavailable").getString(), id.toLowerCase(Locale.ROOT), false));
        registry.sort(Comparator.comparing(Entry::search));
        buildControls();
    }
    private void buildControls() {
        clearWidgets(); controls.clear();
        button(20, 64, 218, 30, text("assigned"), null, () -> mode(false), () -> !adding);
        button(246, 64, 218, 30, text("add"), UiPaint.Icon.PLUS, () -> mode(true), () -> adding);
        search = field(26, 104, 432, query, 128, value -> { query = value; page = 0; rebuildRows(); });
        rebuildRows();
        setInitialFocus(search);
    }
    private void mode(boolean add) { adding = add; page = 0; query = ""; buildControls(); }
    private void rebuildRows() {
        while (controls.size() > 3) { var widget = controls.removeLast(); removeWidget(widget); }
        String filter = query.strip().toLowerCase(Locale.ROOT);
        matches = registry.stream().filter(item -> (adding ? item.available : config().itemHighlights().colors.containsKey(item.id)) && item.search.contains(filter)).toList();
        page = Math.clamp(page, 0, Math.max(0, (matches.size() - 1) / ROWS));
        int end = Math.min(matches.size(), (page + 1) * ROWS);
        for (int index = page * ROWS; index < end; index++) {
            Entry item = matches.get(index); int y = 146 + (index % ROWS) * 43;
            var row = button(20, y, 444, 39, Component.empty(), null, () -> {
                selected = item.id;
                if (adding && !config().itemHighlights().colors.containsKey(item.id) && config().itemHighlights().colors.size() < ItemHighlightRules.LIMIT)
                    config().setItemHighlights(config().itemHighlights().with(item.id, ItemHighlightRules.Tier.UNCOMMON.color));
                rebuildRows();
            }, () -> selected.equals(item.id));
            row.active = !adding || config().itemHighlights().colors.containsKey(item.id) || config().itemHighlights().colors.size() < ItemHighlightRules.LIMIT;
            if (!row.active) row.setTooltip(Tooltip.create(text("limit", ItemHighlightRules.LIMIT)));
        }
        button(20, 458, 70, 28, Component.literal("←"), null, () -> { page--; rebuildRows(); }, null).active = page > 0;
        button(394, 458, 70, 28, Component.literal("→"), null, () -> { page++; rebuildRows(); }, null).active = end < matches.size();
        chosen = registry.stream().filter(item -> item.id.equals(selected)).findFirst().orElse(null);
        for (int index = 0; index < ItemHighlightRules.Tier.values().length; index++) {
            var tier = ItemHighlightRules.Tier.values()[index];
            button(492 + index % 2 * 126, 236 + index / 2 * 38, 120, 32, text(tier.name().toLowerCase(Locale.ROOT)), null, () -> color(tier.color), () -> config().itemHighlights().colors.getOrDefault(selected, -1) == tier.color).active = chosen != null;
        }
        button(492, 318, 246, 30, text("custom"), UiPaint.Icon.RENDER, () -> minecraft.gui.setScreen(ColorEditorScreen.single(this, text("custom"), config().itemHighlights().colors.getOrDefault(selected, ItemHighlightRules.Tier.UNCOMMON.color), this::color)), null).active = chosen != null;
        button(492, 356, 246, 30, text("remove"), null, () -> { config().setItemHighlights(config().itemHighlights().without(selected)); rebuildRows(); }, null).active = config().itemHighlights().colors.containsKey(selected);
        button(492, 458, 118, 28, text("reset"), UiPaint.Icon.RESET, () -> minecraft.gui.setScreen(new ConfirmScreen(yes -> { if (yes) { config().setItemHighlights(ItemHighlightRules.defaults()); } minecraft.gui.setScreen(this); }, text("reset"), text("reset_confirm"))), null);
        button(620, 458, 118, 28, UiI18n.tr("toolvisuals.ui.back"), UiPaint.Icon.BACK, this::onClose, null);
        button(492, 396, 246, 30, text("enabled"), null, () -> config().setActive(VisualModule.ITEM_HIGHLIGHTS, !config().active(VisualModule.ITEM_HIGHLIGHTS)), () -> config().active(VisualModule.ITEM_HIGHLIGHTS));
        button(20, 412, 444, 32, text("appearance"), UiPaint.Icon.RENDER, () -> minecraft.gui.setScreen(new ItemHighlightAppearanceScreen(this)), null);
        int index = 0;
        for (var setting : new VisualSetting[]{VisualSetting.ITEM_HIGHLIGHT_INVENTORY, VisualSetting.ITEM_HIGHLIGHT_HOTBAR, VisualSetting.ITEM_HIGHLIGHT_HANDS}) {
            button(20 + index * 246, 508, 236, 28, setting.label(), null, () -> config().setValue(setting, config().flag(setting) ? 0 : 1), () -> config().flag(setting)); index++;
        }
    }
    private void color(int color) {
        if (!config().itemHighlights().colors.containsKey(selected) && config().itemHighlights().colors.size() >= ItemHighlightRules.LIMIT) return;
        config().setItemHighlights(config().itemHighlights().with(selected, color)); rebuildRows();
    }
    @Override protected void paint(GuiGraphicsExtractor g, int mx, int my) {
        UiPaint.text(g, text("search_hint"), px + 28, py + 136, .55f, palette.muted, 1);
        if (matches.isEmpty()) UiPaint.centered(g, text("empty").getString(), px + 20, py + 232, 444, 44, .8f, palette.muted, 1);
        UiPaint.centered(g, text("page", page + 1, Math.max(1, (matches.size() + ROWS - 1) / ROWS), matches.size()).getString(), px + 98, py + 458, 288, 28, .7f, palette.muted, 1);
        UiPaint.rounded(g, px + 492, py + 64, 246, 158, 10, palette.field);
        var item = chosen;
        if (item != null) {
            g.pose().pushMatrix(); g.pose().translate(px + 590, py + 78); g.pose().scale(3, 3); SmoothHudItems.item(g, item.stack, 0, 0); g.pose().popMatrix();
            UiPaint.centered(g, UiPaint.fit(g, Component.literal(item.name), 226, .85f), px + 502, py + 144, 226, 28, .85f, palette.text, 1);
            Integer color = config().itemHighlights().colors.get(item.id);
            UiPaint.centered(g, color == null ? text("none").getString() : String.format(Locale.ROOT, "#%06X", color), px + 502, py + 177, 226, 24, .8f, color == null ? palette.muted : 0xff000000 | color, 1);
        }
    }
    @Override protected void overlay(GuiGraphicsExtractor g) {
        for (int index = 0; index < ItemHighlightRules.Tier.values().length; index++)
            UiPaint.rounded(g, px + 500 + index % 2 * 126, py + 264 + index / 2 * 38, 104, 2, 1, 0xff000000 | ItemHighlightRules.Tier.values()[index].color);
        int end = Math.min(matches.size(), (page + 1) * ROWS);
        for (int index = page * ROWS; index < end; index++) {
            Entry item = matches.get(index); int y = py + 146 + index % ROWS * 43;
            Integer color = config().itemHighlights().colors.get(item.id);
            if (color != null) UiPaint.rounded(g, px + 450, y + 13, 10, 10, 5, 0xff000000 | color);
            SmoothHudItems.item(g, item.stack, px + 30, y + 10);
            UiPaint.text(g, UiPaint.fit(g, Component.literal(item.name), 374, .75f), px + 58, y + 5, .75f, palette.text, 1);
            UiPaint.text(g, UiPaint.fit(g, Component.literal(item.id), 374, .55f), px + 58, y + 22, .55f, palette.muted, 1);
        }
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x / scale < px + 480 && vertical != 0) { page += vertical < 0 ? 1 : -1; rebuildRows(); return true; }
        return super.mouseScrolled(x / scale, y / scale, horizontal, vertical);
    }
}

package ru.he3hauka.toolvisuals.client.items.highlight.ui;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ItemHighlightAppearanceScreen extends VisualEditorScreen {
    public ItemHighlightAppearanceScreen(Screen parent) { super(parent, text("appearance")); }
    private static Component text(String key) { return UiI18n.tr("toolvisuals.item_highlights." + key); }
    @Override protected void init() {
        frame(620, 358);
        adjustment(20, 94, ITEM_HIGHLIGHT_STRENGTH, true);
        adjustment(326, 94, ITEM_HIGHLIGHT_RADIUS, false);
        button(20, 148, 274, 32, ITEM_HIGHLIGHT_SLOT_FILL.label(), null,
                () -> ToolVisualsClient.CONFIG.setValue(ITEM_HIGHLIGHT_SLOT_FILL, ToolVisualsClient.CONFIG.flag(ITEM_HIGHLIGHT_SLOT_FILL) ? 0 : 1),
                () -> ToolVisualsClient.CONFIG.flag(ITEM_HIGHLIGHT_SLOT_FILL));
        adjustment(326, 148, ITEM_HIGHLIGHT_SLOT_OPACITY, true);
        adjustment(20, 250, ITEM_HIGHLIGHT_HAND_STRENGTH, true);
        adjustment(326, 250, ITEM_HIGHLIGHT_HAND_RADIUS, false);
        button(440, 310, 160, 28, UiI18n.tr("toolvisuals.ui.back"), UiPaint.Icon.BACK, this::onClose, null);
    }
    private void adjustment(int x, int y, VisualSetting setting, boolean percent) {
        slider(x, y, 274, setting.label(), setting.min, setting.max, setting.step,
                () -> ToolVisualsClient.CONFIG.value(setting), value -> ToolVisualsClient.CONFIG.setValue(setting, value),
                value -> percent ? Math.round(value * 100) + "%" : String.format(Locale.ROOT, "%.1f", value));
    }
    @Override protected void paint(GuiGraphicsExtractor graphics, int mx, int my) {
        UiPaint.text(graphics, text("slots"), px + 20, py + 64, .85f, palette.text, 1);
        graphics.fill(px + 20, py + 202, px + 600, py + 203, palette.border);
        UiPaint.text(graphics, text("hands_section"), px + 20, py + 220, .85f, palette.text, 1);
    }
}

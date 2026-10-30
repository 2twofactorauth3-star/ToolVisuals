package ru.he3hauka.toolvisuals.client.items.shulkertooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class ShulkerTooltipComponent implements ClientTooltipComponent {
    private static final int CELL = 18, PADDING = 4, FOOTER = 12, GAP = 2;
    public static final int WIDTH = ShulkerPreview.COLUMNS * CELL + PADDING * 2;
    public static final int HEIGHT = ShulkerPreview.ROWS * CELL + PADDING * 2 + FOOTER + GAP;
    private final ShulkerPreview preview;

    public ShulkerTooltipComponent(ShulkerPreview preview) { this.preview = preview; }
    @Override public int getWidth(Font font) { return WIDTH; }
    @Override public int getHeight(Font font) { return HEIGHT; }

    @Override public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        var palette = new UiPalette(ToolVisualsClient.CONFIG.lightTheme() ? 1 : 0);
        int accent = preview.color == 0 ? palette.accent : preview.color;
        int panel = UiPalette.mix(palette.inset, accent, .16f), slotColor = UiPalette.mix(palette.raised, accent, .1f);
        int left = x + Math.max(0, (width - WIDTH) / 2), top = y + GAP;
        UiPaint.rounded(graphics, left, top, WIDTH, HEIGHT - GAP, 5, panel);
        for (int slot = 0; slot < ShulkerPreview.SLOTS; slot++) {
            int sx = left + PADDING + slot % ShulkerPreview.COLUMNS * CELL;
            int sy = top + PADDING + slot / ShulkerPreview.COLUMNS * CELL;
            UiPaint.rounded(graphics, sx, sy, CELL - 1, CELL - 1, 2, slotColor);
            var item = preview.item(slot);
            if (item.isEmpty()) continue;
            SmoothHudItems.item(graphics, item, sx + 1, sy + 1);
            SmoothHudItems.decorations(graphics, item, sx + 1, sy + 1, palette);
        }
        var footer = preview.occupied == 0 ? UiI18n.tr("toolvisuals.shulker_tooltip.empty")
                : UiI18n.tr("toolvisuals.shulker_tooltip.slots", preview.occupied, ShulkerPreview.SLOTS);
        UiPaint.baseline(graphics, footer.getString(), left + PADDING, top + HEIGHT - GAP - PADDING, .6f,
                UiPalette.mix(palette.text, accent, .25f), 1);
    }
}

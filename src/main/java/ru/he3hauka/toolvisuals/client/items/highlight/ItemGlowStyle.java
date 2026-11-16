package ru.he3hauka.toolvisuals.client.items.highlight;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ItemGlowStyle {
    private static final long START = System.nanoTime();
    private ItemGlowStyle() {}
    public static float phase() { return (System.nanoTime() - START) % 6_000_000_000L / 6_000_000_000f; }
    public static float sheen(float phase, float x, float y) {
        if (phase < 0) return 0;
        float wave = .5f + .5f * (float)Math.sin((x * .55f - y * .8f - phase) * Math.PI * 2);
        return wave * wave * wave * wave;
    }
    public static int glint(int color, float x, float y, float phase) {
        return tint(color, 1, .16f * sheen(phase, x, y), 1);
    }
    public static int tint(int color, float brightness, float white, float opacity) {
        int red = color >> 16 & 255, green = color >> 8 & 255, blue = color & 255;
        if ((red | green | blue) == 0) white = 0;
        red = Math.clamp(Math.round(red * brightness + (255 - red) * white), 0, 255);
        green = Math.clamp(Math.round(green * brightness + (255 - green) * white), 0, 255);
        blue = Math.clamp(Math.round(blue * brightness + (255 - blue) * white), 0, 255);
        int alpha = Math.clamp(Math.round((color >>> 24) * opacity), 0, 255);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }
    public static void slot(GuiGraphicsExtractor graphics, int x, int y) {
        int color = ItemHighlights.slotColor();
        if (color >>> 24 == 0) return;
        float phase = ItemHighlights.extracting().phase();
        float top = sheen(phase, x / 180f, y / 100f), bottom = sheen(phase, x / 180f, (y + 18) / 100f);
        int upper = tint(color, .78f, .08f * top, .75f), lower = tint(color, 1, .12f * bottom, 1.4f);
        graphics.fillGradient(x - 1, y - 1, x + 17, y + 17, upper, lower);
        graphics.fillGradient(x - 1, y - 1, x, y + 17, tint(upper, 1, 0, 1.1f), tint(lower, 1, 0, 1.1f));
        graphics.fillGradient(x + 16, y - 1, x + 17, y + 17, tint(upper, 1, 0, 1.1f), tint(lower, 1, 0, 1.1f));
        graphics.fill(x - 1, y - 1, x + 17, y, tint(color, 1, .08f * top, .9f));
        graphics.fill(x - 1, y + 16, x + 17, y + 17, tint(color, 1, .15f + .15f * bottom, 1.85f));
    }
}

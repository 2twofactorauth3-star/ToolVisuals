package ru.he3hauka.toolvisuals.client.hud.tab.font;

import net.minecraft.client.gui.Font;
import ru.he3hauka.toolvisuals.mixin.gui.FontAccessor;

public final class TabFonts {
    private static Font source;
    private static final Font[] FONTS = new Font[TabFontRaster.values().length];

    private TabFonts() {}

    public static Font get(Font original) {
        return get(original, TabFontRaster.NORMAL);
    }

    public static Font get(Font original, TabFontRaster raster) {
        if (source != original) {
            java.util.Arrays.fill(FONTS, null);
            source = original;
        }
        Font font = FONTS[raster.ordinal()];
        if (font == null) {
            font = new Font(new TabFontProvider(((FontAccessor)(Object)original).toolvisuals$fontProvider(), raster));
            FONTS[raster.ordinal()] = font;
        }
        return font;
    }

    public static void invalidate() { source = null; java.util.Arrays.fill(FONTS, null); }
}

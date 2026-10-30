package ru.he3hauka.toolvisuals.client.hud.tab.font;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.FontDescription;

final class TabFontProvider implements Font.Provider {
    private final Font.Provider original;
    private final FontDescription face;
    private GlyphSource previousOriginal, previousSmooth, merged;

    TabFontProvider(Font.Provider original, TabFontRaster raster) { this.original = original; this.face = raster.face; }

    @Override public GlyphSource glyphs(FontDescription description) {
        if (!FontDescription.DEFAULT.equals(description)) return original.glyphs(description);
        var standard = original.glyphs(description);
        var smooth = original.glyphs(face);
        if (standard != previousOriginal || smooth != previousSmooth || merged == null) {
            previousOriginal = standard;
            previousSmooth = smooth;
            merged = new TabGlyphSource(standard, smooth);
        }
        return merged;
    }

    @Override public EffectGlyph effect() { return original.effect(); }
}

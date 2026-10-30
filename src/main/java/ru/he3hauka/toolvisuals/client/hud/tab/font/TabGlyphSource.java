package ru.he3hauka.toolvisuals.client.hud.tab.font;

import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.util.RandomSource;

final class TabGlyphSource implements GlyphSource {
    private final GlyphSource original, smooth;

    TabGlyphSource(GlyphSource original, GlyphSource smooth) {
        this.original = original;
        this.smooth = smooth;
    }

    @Override public BakedGlyph getGlyph(int codePoint) {
        return (smoothText(codePoint) ? smooth : original).getGlyph(codePoint);
    }

    @Override public BakedGlyph getRandomGlyph(RandomSource random, int width) {
        return smooth.getRandomGlyph(random, width);
    }

    static boolean smoothText(int codePoint) {
        return TabGlyphRules.smoothText(codePoint);
    }
}

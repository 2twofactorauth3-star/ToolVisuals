package ru.he3hauka.toolvisuals.client.hud.tab.font;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;

final class TabGlyphSource extends FontSet {
    private final FontSet original,smooth;
    TabGlyphSource(FontSet original,FontSet smooth,net.minecraft.client.renderer.texture.TextureManager textures){super(textures,ResourceLocation.parse("toolvisuals:tab_merged"));this.original=original;this.smooth=smooth;}
    @Override public GlyphInfo getGlyphInfo(int point,boolean filter){return (smoothText(point)?smooth:original).getGlyphInfo(point,filter);}
    @Override public BakedGlyph getGlyph(int point){return (smoothText(point)?smooth:original).getGlyph(point);}
    @Override public BakedGlyph getRandomGlyph(GlyphInfo info){return smooth.getRandomGlyph(info);}
    @Override public BakedGlyph whiteGlyph(){return smooth.whiteGlyph();}
    static boolean smoothText(int point){return TabGlyphRules.smoothText(point);}
}

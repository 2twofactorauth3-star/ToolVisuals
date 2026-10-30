package ru.he3hauka.toolvisuals.client.hud.tab.font;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

final class TabFontProvider implements Function<ResourceLocation,FontSet> {
    private final Function<ResourceLocation,FontSet> original;
    private final ResourceLocation face;
    private final Supplier<TextureManager> textures;
    private FontSet previousOriginal,previousSmooth,merged;
    TabFontProvider(Function<ResourceLocation,FontSet> original,TabFontRaster raster){this(original,raster,()->Minecraft.getInstance().getTextureManager());}
    TabFontProvider(Function<ResourceLocation,FontSet> original,TabFontRaster raster,Supplier<TextureManager> textures){this.original=original;face=raster.face;this.textures=textures;}
    @Override public FontSet apply(ResourceLocation id){if(!id.equals(net.minecraft.network.chat.Style.DEFAULT_FONT))return original.apply(id);var standard=original.apply(id);var smooth=original.apply(face);if(standard!=previousOriginal||smooth!=previousSmooth||merged==null){previousOriginal=standard;previousSmooth=smooth;merged=new TabGlyphSource(standard,smooth,textures.get());}return merged;}
}

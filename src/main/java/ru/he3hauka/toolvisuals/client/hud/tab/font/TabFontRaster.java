package ru.he3hauka.toolvisuals.client.hud.tab.font;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public enum TabFontRaster {
    TINY("tab_tiny", 1), SMALL("tab_small", 2), NORMAL("tab", 4);

    private static final TabFontRaster[] RASTERS = values();
    public final FontDescription face;
    public final int oversample;
    private final String atlasPrefix;

    TabFontRaster(String path, int oversample) {
        face = new FontDescription.Resource(Identifier.fromNamespaceAndPath("toolvisuals", path));
        atlasPrefix = "toolvisuals:" + path + "/";
        this.oversample = oversample;
    }

    public static TabFontRaster select(double pixelsPerUnit) {
        return pixelsPerUnit >= 2 ? NORMAL : pixelsPerUnit >= 1 ? SMALL : TINY;
    }

    public static boolean ownsAtlas(String label) {
        if (label == null) return false;
        for (var raster : RASTERS) if (label.startsWith(raster.atlasPrefix)) return true;
        return false;
    }
}

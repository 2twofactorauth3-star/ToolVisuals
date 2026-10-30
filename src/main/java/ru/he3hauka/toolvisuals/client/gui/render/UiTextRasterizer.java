package ru.he3hauka.toolvisuals.client.gui.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.he3hauka.toolvisuals.client.gui.text.UiTextStyle;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.cache.BoundedCache;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_UI_METRICS;

public final class UiTextRasterizer {
    private static final Font FACE = load();
    private static final FontRenderContext CONTEXT = new FontRenderContext(null, true, true);
    private record Key(int pixels,UiTextStyle style) {}
    private static final ConcurrentHashMap<Key, Metrics> METRICS = new ConcurrentHashMap<>();
    private UiTextRasterizer() {}

    private static Font load() {
        try (var stream = UiTextRasterizer.class.getResourceAsStream("/assets/toolvisuals/font/manrope.ttf")) {
            if (stream == null) throw new IllegalStateException("Missing Manrope");
            return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception exception) { throw new IllegalStateException("Cannot load UI font", exception); }
    }

    public static float width(String text) { return metrics(15).width(text); }

    public static final class Metrics {
        public final int baseline, capHeight, height;
        public final float capExtent;
        private final Font font;
        private final BoundedCache<String,Float> widths = new BoundedCache<>(512);
        private Metrics(int pixels,UiTextStyle style) {
            font=FACE.deriveFont(Map.of(TextAttribute.SIZE,(float)pixels,TextAttribute.WEIGHT,style.weight));
            var line = font.getLineMetrics("Hg", CONTEXT);
            baseline = (int)Math.ceil(line.getAscent());
            capExtent=(float)font.createGlyphVector(CONTEXT,"H").getVisualBounds().getHeight();
            capHeight = (int)Math.ceil(capExtent);
            height = baseline + (int)Math.ceil(line.getDescent() + line.getLeading());
        }
        public float width(String text) {
            boolean cached = Optimizations.enabled(OPT_UI_METRICS);
            if (cached) { Float width = widths.get(text); if (width != null) return width; }
            float width = (float)font.getStringBounds(text, CONTEXT).getWidth();
            if (cached) widths.put(text,width); return width;
        }
        public float centeredBaseline(float top, float height, float density) {
            return top + (height + capHeight / density) / 2;
        }
    }

    public static Metrics metrics(int pixels) { return metrics(pixels,UiTextStyle.REGULAR); }
    public static Metrics metrics(int pixels,UiTextStyle style) {
        return METRICS.computeIfAbsent(new Key(pixels,style),key->new Metrics(key.pixels,key.style));
    }

    public static BufferedImage rasterize(String text, int pixels) {
        return rasterize(text,pixels,UiTextStyle.REGULAR);
    }

    public static BufferedImage rasterize(String text,int pixels,UiTextStyle style) {
        Metrics metrics=metrics(pixels,style);
        Font font = metrics.font;
        int width = Math.max(1, (int)Math.ceil(metrics.width(text)) + 6);
        int height = metrics.height + 4;
        var image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setFont(font); graphics.setColor(Color.WHITE);
        graphics.drawString(text, 2, 2 + metrics.baseline);
        graphics.dispose();
        return image;
    }

}

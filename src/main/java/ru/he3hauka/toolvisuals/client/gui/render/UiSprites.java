package ru.he3hauka.toolvisuals.client.gui.render;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.client.gui.surface.RoundedGlow;
import ru.he3hauka.toolvisuals.client.gui.surface.UiSurface;
import ru.he3hauka.toolvisuals.client.gui.text.TextRasterSizing;

public final class UiSprites {
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();

    private UiSprites() {}

    public static void colorWheel(GuiGraphicsExtractor g,float x,float y,float size) {
        int pixels=256;
        var id=texture("color_wheel",()->UiRasterizer.colorWheel(pixels),true);
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(size/pixels,size/pixels);
        g.blit(RenderPipelines.GUI_TEXTURED,id,0,0,0,0,pixels,pixels,pixels,pixels,0xffffffff);g.pose().popMatrix();
    }

    public static final class SmoothTexture extends DynamicTexture {
        public SmoothTexture(String label, NativeImage pixels) {
            super(() -> label, pixels);
            sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }
    }

    public static float pixelsPerUnit(GuiGraphicsExtractor graphics) {
        var pose = graphics.pose();
        var window=Minecraft.getInstance().getWindow();
        return TextRasterSizing.density(pose.m00(),pose.m01(),pose.m10(),pose.m11(),
                TextRasterSizing.pixelRatio(window.getWidth(),graphics.guiWidth(),window.getGuiScale()),TextRasterSizing.pixelRatio(window.getHeight(),graphics.guiHeight(),window.getGuiScale()));
    }
    static void clear(){var manager=Minecraft.getInstance().getTextureManager();for(var id:TEXTURES.values())manager.release(id);TEXTURES.clear();}

    private static Identifier texture(String name, Supplier<BufferedImage> rasterize) {
        return texture(name,rasterize,false);
    }
    private static Identifier texture(String name, Supplier<BufferedImage> rasterize,boolean colored) {
        return TEXTURES.computeIfAbsent(name, key -> {
            BufferedImage image = rasterize.get();
            NativeImage pixels = new NativeImage(image.getWidth(), image.getHeight(), false);
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                int alpha = image.getRGB(x, y) >>> 24;
                 
                pixels.setPixel(x, y, colored ? image.getRGB(x,y) : alpha << 24 | 0xffffff);
            }
            Identifier id = Identifier.fromNamespaceAndPath("toolvisuals", "ui_cache/" + key);
            Minecraft.getInstance().getTextureManager().register(id, new SmoothTexture(id.toString(), pixels));
            return id;
        });
    }

    static void icon(GuiGraphicsExtractor graphics, UiPaint.Icon icon, float x, float y, float size, int color) {
        int pixels = UiRasterSizes.icon(Math.round(size * pixelsPerUnit(graphics)));
        Identifier texture = texture("icon_" + icon.name().toLowerCase(java.util.Locale.ROOT) + "_" + pixels,
                () -> UiRasterizer.icon(icon, pixels));
        blit(graphics, texture, x, y, size, size, 0, 0, pixels, pixels, pixels, color);
    }

    static void material(GuiGraphicsExtractor g,UiSurface surface,boolean light,float x,float y,float width,float height,float scale,float opacity) {
        if(width<=0||height<=0||scale<=0||opacity<.005f)return;
        var id=texture("surface_"+surface.name().toLowerCase(java.util.Locale.ROOT)+(light?"_light":"_dark"),()->surface.rasterize(light),true);
        float padding=surface.padding*scale;
        float[] xs={x-padding,x+surface.radius*scale,x+width-surface.radius*scale,x+width+padding};
        float[] ys={y-padding,y+surface.radius*scale,y+height-surface.radius*scale,y+height+padding};
        int[] source={0,surface.corner,surface.pixels-surface.corner,surface.pixels};
        int color=UiPaint.alpha(0xffffffff,opacity);
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)
            blit(g,id,xs[col],ys[row],xs[col+1]-xs[col],ys[row+1]-ys[row],source[col],source[row],
                    source[col+1]-source[col],source[row+1]-source[row],surface.pixels,color);
    }

    static void clippedGlow(GuiGraphicsExtractor g,float x,float y,float width,float height,float corner,float cx,float cy,float radius,int color,float opacity) {
        if(radius<=0||opacity<.005f)return;
        var id=texture("spotlight",RoundedGlow::rasterize);
        float ratio=RoundedGlow.PIXELS/(radius*2);
        RoundedGlow.visit(x,y,width,height,corner,cx,cy,radius,(left,top,w,h)->
                blit(g,id,left,top,w,h,(left-cx+radius)*ratio,(top-cy+radius)*ratio,
                        Math.max(1,Math.round(w*ratio)),Math.max(1,Math.round(h*ratio)),RoundedGlow.PIXELS,UiPaint.alpha(color,opacity)));
    }
    static void radialSegment(GuiGraphicsExtractor g,float x,float y,float inner,float outer,int index,int count,int color) {
        int pixels=UiRasterSizes.mask(Math.round(outer*2*pixelsPerUnit(g))),ratio=Math.round(inner/outer*1000);
        var id=texture("radial_"+pixels+"_"+ratio+"_"+count,()->UiRasterizer.radialSegment(pixels,ratio/1000f,0,count));
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().rotate((float)(index*Math.TAU/count));
        blit(g,id,-outer,-outer,outer*2,outer*2,0,0,pixels,pixels,pixels,color);g.pose().popMatrix();
    }

    static void corner(GuiGraphicsExtractor graphics, float x, float y, float radius, int quadrant, int color) {
        int half = UiRasterSizes.corner(Math.round(radius * pixelsPerUnit(graphics)));
        int pixels = half * 2;
        Identifier texture = texture("circle_" + pixels, () -> UiRasterizer.circle(pixels));
        blit(graphics, texture, x, y, radius, radius, quadrant % 2 * half, quadrant / 2 * half,
                half, half, pixels, color);
    }
    static void rounded(GuiGraphicsExtractor g, float x, float y, float width, float height, float r, int color) {
        int half = UiRasterSizes.corner(Math.round(r * pixelsPerUnit(g))), circle = half * 2, pixels = circle + 4;
        Identifier texture = texture("rounded_" + circle, () -> {
            var image = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_ARGB);
            var drawing = image.createGraphics(); drawing.drawImage(UiRasterizer.circle(circle), 0, 0, null); drawing.dispose();
             
            for (int sy = circle + 2; sy < pixels; sy++) for (int sx = circle + 2; sx < pixels; sx++) image.setRGB(sx, sy, 0xffffffff);
            return image;
        });
        int white = circle + 3;
        if (height > r * 2) blit(g, texture, x, y + r, width, height - r * 2, white, white, 0, 0, pixels, color);
        if (width > r * 2) {
            blit(g, texture, x + r, y, width - r * 2, r, white, white, 0, 0, pixels, color);
            blit(g, texture, x + r, y + height - r, width - r * 2, r, white, white, 0, 0, pixels, color);
        }
        for (int q = 0; q < 4; q++) blit(g, texture, x + (q % 2 == 0 ? 0 : width - r),
                y + (q < 2 ? 0 : height - r), r, r, q % 2 * half, q / 2 * half, half, half, pixels, color);
    }
    public static void ring(GuiGraphicsExtractor graphics, float x, float y, float radius, float width, int color) {
        float diameter = radius * 2 + width;
         
        int stroke = Math.clamp(Math.round(width / diameter * 128), 1, 126);
        Identifier id = texture("ring_" + stroke, () -> UiRasterizer.ring(128, stroke));
        blit(graphics, id, x - diameter / 2, y - diameter / 2, diameter, diameter, 0, 0, 128, 128, 128, color);
    }

    private static void blit(GuiGraphicsExtractor graphics, Identifier texture, float x, float y, float width, float height,
                             float u,float v,int sourceWidth,int sourceHeight,int pixels,int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(width, height);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, u, v, 1, 1,
                sourceWidth, sourceHeight, pixels, pixels, color);
        graphics.pose().popMatrix();
    }
}

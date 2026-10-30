package ru.he3hauka.toolvisuals.client.gui.render;

import java.util.LinkedHashMap;
import java.util.function.DoubleToIntFunction;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;

import ru.he3hauka.toolvisuals.client.gui.text.TextRasterSizing;
import ru.he3hauka.toolvisuals.client.gui.text.UiTextStyle;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;

final class UiText {
    private record Key(String text,int pixels,UiTextStyle style) {}
    private static final class Line {
        public final Identifier texture;
        public final int width, height;
        Line(Identifier texture, int width, int height) { this.texture = texture; this.width = width; this.height = height; }
    }
    private static final LinkedHashMap<Key, Line> CACHE = new LinkedHashMap<>(128, .75f, true);
    private static long sequence;
    private static long bytes;
    private static final long BYTE_BUDGET=24L*1024*1024;
    private UiText() {}

    static int pixels(GuiGraphicsExtractor graphics, float scale) {
        return TextRasterSizing.pixels(scale,UiSprites.pixelsPerUnit(graphics));
    }

    static float width(GuiGraphicsExtractor graphics, String text, float scale) {
        return width(graphics,text,scale,UiTextStyle.REGULAR);
    }

    static float width(GuiGraphicsExtractor graphics,String text,float scale,UiTextStyle style) {
        return UiTextRasterizer.metrics(15,style).width(NameProtect.text(text))*scale;
    }

    static float centeredBaseline(GuiGraphicsExtractor graphics, float y, float height, float scale) {
        return centeredBaseline(graphics,y,height,scale,UiTextStyle.REGULAR);
    }

    static float centeredBaseline(GuiGraphicsExtractor graphics,float y,float height,float scale,UiTextStyle style) {
        return y+(height+UiTextRasterizer.metrics(15,style).capExtent*scale)/2;
    }

    static void draw(GuiGraphicsExtractor graphics, String text, float x, float y, float scale, int color) {
        drawBaseline(graphics,text,x,y+UiTextRasterizer.metrics(15).baseline*scale,scale,color);
    }

    static void drawBaseline(GuiGraphicsExtractor graphics, String text, float x, float baseline, float scale, int color) {
        drawBaseline(graphics,text,x,baseline,scale,color,UiTextStyle.REGULAR);
    }

    static void drawBaseline(GuiGraphicsExtractor graphics,String text,float x,float baseline,float scale,int color,UiTextStyle style) {
        drawBaseline(graphics,text,x,baseline,scale,color,style,null);
    }

    static void drawColoredBaseline(GuiGraphicsExtractor graphics,String text,float x,float baseline,float scale,UiTextStyle style,
                                    DoubleToIntFunction colors,float opacity) {
        drawBaseline(graphics,text,x,baseline,scale,UiPaint.alpha(0xffffffff,opacity),style,colors);
    }

    private static void drawBaseline(GuiGraphicsExtractor graphics,String text,float x,float baseline,float scale,int color,UiTextStyle style,DoubleToIntFunction colors) {
        text = NameProtect.text(text);
        if (text.isEmpty()||!Float.isFinite(scale)||scale<=0) return;
        int pixels=pixels(graphics,scale);
        float density=TextRasterSizing.rasterDensity(pixels,scale);
        Key key=new Key(text,pixels,style);
        Line line = CACHE.get(key);
        if (line == null) {
            var image=UiTextRasterizer.rasterize(text,pixels,style);
            var nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
            for (int iy = 0; iy < image.getHeight(); iy++) for (int ix = 0; ix < image.getWidth(); ix++)
                nativeImage.setPixel(ix,iy,image.getRGB(ix,iy)&0xff000000|0xffffff);
            var id = Identifier.fromNamespaceAndPath("toolvisuals", "ui_text/line_" + sequence++);
            Minecraft.getInstance().getTextureManager().register(id, new UiSprites.SmoothTexture(id.toString(), nativeImage));
            line = new Line(id, image.getWidth(), image.getHeight());
            CACHE.put(key, line);
            bytes+=(long)line.width*line.height*4;
            while (CACHE.size() > 1&&(CACHE.size() > 512||bytes>BYTE_BUDGET)) {
                Line old = CACHE.remove(CACHE.keySet().iterator().next());
                bytes-=(long)old.width*old.height*4;
                Minecraft.getInstance().getTextureManager().release(old.texture);
            }
        }
         
        var pose = graphics.pose();
        var point = pose.transformPosition(x, baseline, new Vector2f());
        var window=Minecraft.getInstance().getWindow();
        float xPixels=TextRasterSizing.pixelRatio(window.getWidth(),graphics.guiWidth(),window.getGuiScale());
        float yPixels=TextRasterSizing.pixelRatio(window.getHeight(),graphics.guiHeight(),window.getGuiScale());
        var offset=TextRasterSizing.snap(point.x,point.y,pose.m00(),pose.m01(),pose.m10(),pose.m11(),xPixels,yPixels);
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + offset.x() - 2 / density,
                baseline + offset.y() - (2 + UiTextRasterizer.metrics(pixels,style).baseline) / density);
        graphics.pose().scale(1 / density, 1 / density);
        if(colors==null)graphics.blit(UiPipelines.RASTER_TEXT,line.texture,0,0,0,0,line.width,line.height,line.width,line.height,color);
        else {
            int segments=Math.min(96,Math.max(24,line.width/3));
            for(int segment=0;segment<segments;segment++) {
                int start=segment*line.width/segments,end=(segment+1)*line.width/segments;
                if(end==start)continue;
                double position=Math.clamp(((start+end)/2d-2)/Math.max(1,line.width-6),0,1);
                int tint=UiPaint.alpha(colors.applyAsInt(position),(color>>>24)/255f);
                graphics.blit(UiPipelines.RASTER_TEXT,line.texture,start,0,start,0,end-start,line.height,line.width,line.height,tint);
            }
        }
        graphics.pose().popMatrix();
    }
    static void clear(){var manager=Minecraft.getInstance().getTextureManager();for(var line:CACHE.values())manager.release(line.texture);CACHE.clear();bytes=0;}
}

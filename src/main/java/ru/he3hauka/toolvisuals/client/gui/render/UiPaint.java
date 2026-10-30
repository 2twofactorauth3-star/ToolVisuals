package ru.he3hauka.toolvisuals.client.gui.render;

import java.util.function.DoubleToIntFunction;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.gui.text.UiTextStyle;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;

public final class UiPaint {
    public enum Icon { CLOSE, SEARCH, SUN, MOON, KEYBOARD, BACK, ARROW, WORLD, PLAYER, RENDER, HUD, CAMERA, EYE, SETTINGS, RESET, CHECK, STAR, STAR_FILLED, PLUS, PING, CLOCK, FPS, VERSIONS, REALMS, CHEVRON, MUSIC, PLAY, PAUSE, PREVIOUS, NEXT, STOP, SHUFFLE, REPEAT, SPEAKER, SPEAKER_OFF }

    private UiPaint() {}

    public static boolean hovered(GuiGraphicsExtractor graphics, net.minecraft.client.gui.components.AbstractWidget widget, int mouseX, int mouseY) {
        var point = graphics.pose().transformPosition(mouseX, mouseY, new org.joml.Vector2f());
        return widget.visible && mouseX >= widget.getX() && mouseX < widget.getX() + widget.getWidth()
                && mouseY >= widget.getY() && mouseY < widget.getY() + widget.getHeight()
                && graphics.containsPointInScissor((int)Math.floor(point.x), (int)Math.floor(point.y));
    }

    public static int alpha(int color, float opacity) {
        return (color & 0xffffff) | (Math.round((color >>> 24) * Math.clamp(opacity, 0, 1)) << 24);
    }
    public static void radialSegment(GuiGraphicsExtractor g,float x,float y,float inner,float outer,int index,int count,int color){UiSprites.radialSegment(g,x,y,inner,outer,index,count,color);}

    public static void material(GuiGraphicsExtractor g,ru.he3hauka.toolvisuals.client.gui.surface.UiSurface surface,boolean light,
                                float x,float y,float width,float height,float scale,float opacity) {
        UiSprites.material(g,surface,light,x,y,width,height,scale,opacity);
    }

    public static void clippedGlow(GuiGraphicsExtractor g,float x,float y,float width,float height,float corner,float cx,float cy,float radius,int color,float opacity) {
        UiSprites.clippedGlow(g,x,y,width,height,corner,cx,cy,radius,color,opacity);
    }

    private static void rectangle(GuiGraphicsExtractor graphics, float x, float y, float width, float height, int color) {
        if (width <= 0 || height <= 0) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(width, height);
        graphics.fill(0, 0, 1, 1, color);
        graphics.pose().popMatrix();
    }

    public static void rounded(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float radius, int color) {
        if (width <= 0 || height <= 0 || color >>> 24 == 0) return;
        float r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r == 0) { rectangle(graphics, x, y, width, height, color); return; }
        if (ru.he3hauka.toolvisuals.client.optimization.Optimizations.enabled(
                ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_FAST_HUD)) {
            UiShapeBatch.begin();
            try { UiSprites.rounded(graphics, x, y, width, height, r, color); } finally { UiShapeBatch.end(); }
            return;
        }
        rectangle(graphics, x, y + r, width, height - 2 * r, color);
        rectangle(graphics, x + r, y, width - 2 * r, r, color);
        rectangle(graphics, x + r, y + height - r, width - 2 * r, r, color);
        UiSprites.corner(graphics, x, y, r, 0, color);
        UiSprites.corner(graphics, x + width - r, y, r, 1, color);
        UiSprites.corner(graphics, x, y + height - r, r, 2, color);
        UiSprites.corner(graphics, x + width - r, y + height - r, r, 3, color);
    }

    public static void surface(GuiGraphicsExtractor graphics,float x,float y,float width,float height,float radius,int fill,int border,float opacity){
        rounded(graphics,x,y,width,height,radius,alpha(border,opacity));
        rounded(graphics,x+1,y+1,width-2,height-2,Math.max(0,radius-1),alpha(fill,opacity));
    }

    public static void shadow(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float radius, float opacity) {
        for (int spread = 12; spread >= 1; spread--) {
            rounded(graphics, x - spread, y - spread / 2f + 3, width + spread * 2,
                    height + spread + 3, radius + spread, alpha(0x03000000, opacity));
        }
    }

    public static void text(GuiGraphicsExtractor graphics, Component message, float x, float y, float scale, int color, float opacity) {
        if (opacity < 0.005f) return;
        UiText.draw(graphics, message.getString(), x, y, scale, alpha(color, opacity));
    }

    public static void text(GuiGraphicsExtractor graphics, String message, float x, float y, float scale, int color, float opacity) {
        text(graphics, Component.literal(message), x, y, scale, color, opacity);
    }

    public static String fit(Component message, int width) {
        String value = NameProtect.text(message.getString());
        if (UiTextRasterizer.width(value) <= width) return value;
        int end = value.length();
        while (end > 0 && UiTextRasterizer.width(value.substring(0, end) + "\u2026") > width)
            end = value.offsetByCodePoints(end, -1);
        return value.substring(0, end) + "\u2026";
    }

    public static float textWidth(GuiGraphicsExtractor graphics, String message, float scale) {
        return UiText.width(graphics, message, scale);
    }

    public static float textWidth(GuiGraphicsExtractor graphics,String message,float scale,UiTextStyle style) {
        return UiText.width(graphics,message,scale,style);
    }

    public static float textBaseline(GuiGraphicsExtractor graphics, float y, float height, float scale) {
        return UiText.centeredBaseline(graphics, y, height, scale);
    }

    public static float textBaseline(GuiGraphicsExtractor graphics,float y,float height,float scale,UiTextStyle style) {
        return UiText.centeredBaseline(graphics,y,height,scale,style);
    }

    public static void baseline(GuiGraphicsExtractor graphics, String message, float x, float baseline, float scale, int color, float opacity) {
        baseline(graphics,message,x,baseline,scale,color,opacity,UiTextStyle.REGULAR);
    }

    public static void baseline(GuiGraphicsExtractor graphics,String message,float x,float baseline,float scale,int color,float opacity,UiTextStyle style) {
        if(opacity>=.005f)UiText.drawBaseline(graphics,message,x,baseline,scale,alpha(color,opacity),style);
    }

    public static void coloredBaseline(GuiGraphicsExtractor graphics,String message,float x,float baseline,float scale,UiTextStyle style,DoubleToIntFunction colors,float opacity) {
        if(opacity>=.005f)UiText.drawColoredBaseline(graphics,message,x,baseline,scale,style,colors,opacity);
    }

    public static void centered(GuiGraphicsExtractor graphics, String message, float x, float y, float width, float height,
                                float scale, int color, float opacity) {
        centered(graphics,message,x,y,width,height,scale,color,opacity,UiTextStyle.REGULAR);
    }

    public static void centered(GuiGraphicsExtractor graphics,String message,float x,float y,float width,float height,
                                float scale,int color,float opacity,UiTextStyle style) {
        baseline(graphics,message,x+(width-textWidth(graphics,message,scale,style))/2,
                textBaseline(graphics,y,height,scale,style),scale,color,opacity,style);
    }

    public static String fit(GuiGraphicsExtractor graphics, Component message, float width, float scale) {
        return fit(graphics,message,width,scale,UiTextStyle.REGULAR);
    }

    public static String fit(GuiGraphicsExtractor graphics,Component message,float width,float scale,UiTextStyle style) {
        String text = NameProtect.text(message.getString());
        if (textWidth(graphics,text,scale,style)<=width)return text;
        int end = text.length();
        while(end>0&&textWidth(graphics,text.substring(0,end)+"\u2026",scale,style)>width)
            end = text.offsetByCodePoints(end, -1);
        return end==0&&textWidth(graphics,"\u2026",scale,style)>width?"":text.substring(0,end)+"\u2026";
    }

    public static void toggle(GuiGraphicsExtractor graphics, float x, float y, float progress, UiPalette palette, float opacity) {
        rounded(graphics, x, y, 29, 16, 8, alpha(UiPalette.mix(palette.track, palette.accent, progress), opacity));
        rounded(graphics, x + 2 + progress * 13, y + 2, 12, 12, 6, alpha(0xfffbfaff, opacity));
    }

    public static void icon(GuiGraphicsExtractor graphics, Icon icon, float x, float y, float size, int color, float opacity) {
        if (opacity < 0.005f) return;
        UiSprites.icon(graphics, icon, x, y, size, alpha(color, opacity));
    }
}

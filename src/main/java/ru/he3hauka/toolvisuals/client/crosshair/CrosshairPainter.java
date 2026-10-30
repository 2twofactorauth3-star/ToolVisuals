package ru.he3hauka.toolvisuals.client.crosshair;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.render.UiSprites;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class CrosshairPainter {
    private CrosshairPainter() { }

    public static boolean visibleSetting(VisualSetting setting) {
        var config = ToolVisualsClient.CONFIG;
        int style = (int)config.value(CROSSHAIR_STYLE);
        return switch (setting) {
            case CROSSHAIR_SIZE -> style != 3;
            case CROSSHAIR_CUSTOM_SCALE -> style == 3;
            case CROSSHAIR_GAP, CROSSHAIR_CENTER_DOT -> style != 1 && style != 3;
            case CROSSHAIR_THICKNESS -> style != 1 && style != 3;
            case CROSSHAIR_T_STYLE -> style == 0;
            case CROSSHAIR_OUTLINE_WIDTH -> config.flag(CROSSHAIR_OUTLINE);
            case CROSSHAIR_DOT_SIZE -> style != 1 && style != 3 && config.flag(CROSSHAIR_CENTER_DOT);
            case CROSSHAIR_SPREAD -> config.flag(CROSSHAIR_DYNAMIC);
            case CROSSHAIR_RED, CROSSHAIR_GREEN, CROSSHAIR_BLUE -> false;
            default -> true;
        };
    }

    public static void draw(GuiGraphicsExtractor g, float x, float y, float attackStrength, float opacity) {
        var c = ToolVisualsClient.CONFIG;
        float size = c.number(CROSSHAIR_SIZE), gap = c.number(CROSSHAIR_GAP), thickness = c.number(CROSSHAIR_THICKNESS);
        float spread = c.flag(CROSSHAIR_DYNAMIC) ? (1 - Math.clamp(attackStrength, 0, 1)) * c.number(CROSSHAIR_SPREAD) : 0;
        var styleColor=c.colorStyle(CROSSHAIR_COLOR);double time=ColorStyle.clock();
        int rgb = styleColor.sample(time,0);
        int color = UiPaint.alpha(0xff000000 | rgb, opacity * c.number(CROSSHAIR_OPACITY));
        int outline = UiPaint.alpha(0xff090b10, opacity * c.number(CROSSHAIR_OPACITY));
        float border = c.flag(CROSSHAIR_OUTLINE) ? c.number(CROSSHAIR_OUTLINE_WIDTH) : 0;
        int style = (int)c.value(CROSSHAIR_STYLE);
        if (style == 3) { design(g,c.crosshairDesign(),styleColor,x,y,c.number(CROSSHAIR_CUSTOM_SCALE)*(1+spread/16),border,opacity*c.number(CROSSHAIR_OPACITY),time);return; }
        if (style == 1) dot(g, x, y, size + spread, border, color, outline);
        else if (style == 2) {
            float radius = size + gap + spread;
            if (border > 0) UiSprites.ring(g, x, y, radius, thickness + border * 2, outline);
            UiSprites.ring(g, x, y, radius, thickness, color);
        } else {
            gap += spread;
             
            for (int pass = border > 0 ? 0 : 1; pass < 2; pass++) {
                float extra = pass == 0 ? border : 0;
                int top=pass==0?outline:UiPaint.alpha(0xff000000|styleColor.sample(time,.25),opacity*c.number(CROSSHAIR_OPACITY));
                int bottom=pass==0?outline:UiPaint.alpha(0xff000000|styleColor.sample(time,.75),opacity*c.number(CROSSHAIR_OPACITY));
                int right=pass==0?outline:UiPaint.alpha(0xff000000|styleColor.sample(time,.5),opacity*c.number(CROSSHAIR_OPACITY));
                int left=pass==0?outline:color;
                if (!c.flag(CROSSHAIR_T_STYLE)) bar(g, x - thickness / 2, y - gap - size, thickness, size, extra, top);
                bar(g, x - thickness / 2, y + gap, thickness, size, extra, bottom);
                bar(g, x - gap - size, y - thickness / 2, size, thickness, extra, left);
                bar(g, x + gap, y - thickness / 2, size, thickness, extra, right);
            }
        }
        if (style != 1 && c.flag(CROSSHAIR_CENTER_DOT)) dot(g, x, y, c.number(CROSSHAIR_DOT_SIZE), border, color, outline);
    }
    public static void design(GuiGraphicsExtractor g,CrosshairDesign design,ColorStyle colors,float x,float y,float scale,float border,float opacity,double time) {
        for(int pass=border>0?0:1;pass<2;pass++)for(var run:design.runs) {
            float extra=pass==0?border:0;
            int color=pass==0?0xff090b10:0xff000000|colors.sample(time,(run.x+run.length/2d+run.y)/64);
            bar(g,x+(run.x-CrosshairDesign.CENTER-.5f)*scale,y+(run.y-CrosshairDesign.CENTER-.5f)*scale,run.length*scale,scale,extra,UiPaint.alpha(color,opacity));
        }
    }
    private static void dot(GuiGraphicsExtractor g, float x, float y, float diameter, float border, int color, int outline) {
        if (border > 0) UiPaint.rounded(g, x - diameter / 2 - border, y - diameter / 2 - border, diameter + border * 2, diameter + border * 2, diameter, outline);
        UiPaint.rounded(g, x - diameter / 2, y - diameter / 2, diameter, diameter, diameter / 2, color);
    }
    private static void bar(GuiGraphicsExtractor g, float x, float y, float w, float h, float extra, int color) {
        UiPaint.rounded(g, x - extra, y - extra, w + extra * 2, h + extra * 2, (Math.min(w, h) + extra * 2) / 2, color);
    }
}

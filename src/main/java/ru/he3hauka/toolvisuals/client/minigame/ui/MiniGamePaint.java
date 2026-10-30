package ru.he3hauka.toolvisuals.client.minigame.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.minigame.GameKind;
import ru.he3hauka.toolvisuals.minigame.sling.SlingGame;

final class MiniGamePaint {
    private MiniGamePaint() {}
    static void disk(GuiGraphicsExtractor g, double x, double y, double radius, int color) {
        UiPaint.rounded(g, (float)(x - radius), (float)(y - radius), (float)(radius * 2), (float)(radius * 2), (float)radius, color);
    }
    static void line(GuiGraphicsExtractor g, double x, double y, double endX, double endY, double thickness, int color) {
        double length = Math.hypot(endX - x, endY - y); if (length < .01) return;
        g.pose().pushMatrix(); g.pose().translate((float)x, (float)y); g.pose().rotate((float)Math.atan2(endY - y, endX - x));
        UiPaint.rounded(g, 0, (float)(-thickness / 2), (float)length, (float)thickness, (float)(thickness / 2), color); g.pose().popMatrix();
    }
    static void bird(GuiGraphicsExtractor g, double x, double y, double size) {
        disk(g, x + 1, y + 2, size, 0x55101020); disk(g, x, y, size, 0xffef6473);
        disk(g, x - size * .3, y + size * .33, size * .5, 0xffffb5a1);
        disk(g, x + size * .35, y - size * .25, size * .35, 0xfffefcff);
        disk(g, x + size * .5, y - size * .22, size * .12, 0xff222332);
        line(g, x + size * .8, y, x + size * 1.3, y + size * .1, size * .3, 0xffffcc70);
        line(g, x - size * .2, y - size * .85, x - size * .55, y - size * 1.3, size * .22, 0xffde475e);
    }
    static void pig(GuiGraphicsExtractor g, double x, double y, double size) {
        disk(g, x - size * .55, y - size * .7, size * .35, 0xff65aa82);
        disk(g, x + size * .55, y - size * .7, size * .35, 0xff65aa82);
        disk(g, x, y, size, 0xff91d69b); disk(g, x - size * .38, y - size * .25, size * .23, 0xfff9fff5);
        disk(g, x + size * .38, y - size * .25, size * .23, 0xfff9fff5);
        disk(g, x - size * .3, y - size * .22, size * .1, 0xff253b3d); disk(g, x + size * .3, y - size * .22, size * .1, 0xff253b3d);
        UiPaint.rounded(g, (float)(x - size * .5), (float)(y + size * .05), (float)size, (float)(size * .58), (float)(size * .25), 0xff64b77b);
        disk(g, x - size * .18, y + size * .3, size * .09, 0xff396b57); disk(g, x + size * .18, y + size * .3, size * .09, 0xff396b57);
    }
    static void wood(GuiGraphicsExtractor g, SlingGame.Wood wood, float x, float y) {
        UiPaint.rounded(g, x + (float)wood.x(), y + (float)wood.y(), (float)wood.width(), (float)wood.height(), 3, 0xffb98b60);
        line(g, x + wood.x() + 4, y + wood.y() + 4, x + wood.x() + wood.width() - 4, y + wood.y() + wood.height() - 4, 2, 0xffe8ba80);
    }
    static void preview(GuiGraphicsExtractor g, GameKind kind, float x, float y, UiPalette palette) {
        if (kind == GameKind.ANGRY_BIRDS) {
            line(g, x + 30, y + 82, x + 26, y + 38, 9, 0xffbd916c); line(g, x + 26, y + 47, x + 42, y + 30, 8, 0xffbd916c);
            line(g, x + 25, y + 38, x + 43, y + 37, 4, 0xff794d62); bird(g, x + 37, y + 33, 13);
            for (int i = 0; i < 6; i++) disk(g, x + 61 + i * 13, y + 30 - i * 5 + i * i * 1.25, 2, palette.accentText);
            pig(g, x + 153, y + 68, 17);
        } else {
            int columns = kind == GameKind.TIC_TAC_TOE ? 3 : 5, rows = 3;
            for (int row = 0; row < rows; row++) for (int col = 0; col < columns; col++) {
                float px = x + 28 + col * (kind == GameKind.TIC_TAC_TOE ? 47 : 29), py = y + 9 + row * 29;
                if (kind == GameKind.TIC_TAC_TOE) {
                    UiPaint.rounded(g, px, py, 38, 24, 5, palette.button);
                    if ((row + col) % 3 == 0) { line(g, px + 13, py + 7, px + 25, py + 18, 2.5, palette.accentText); line(g, px + 25, py + 7, px + 13, py + 18, 2.5, palette.accentText); }
                    else if ((row + col) % 3 == 1) { disk(g, px + 19, py + 12, 7, 0xffffc274); disk(g, px + 19, py + 12, 4, palette.button); }
                } else disk(g, px, py + 10, 10, row == 0 && col % 2 == 0 ? palette.track : (row + col) % 2 == 0 ? palette.accent : 0xffffc274);
            }
        }
    }
}

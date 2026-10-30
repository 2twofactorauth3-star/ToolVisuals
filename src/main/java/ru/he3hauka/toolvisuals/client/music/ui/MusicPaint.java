package ru.he3hauka.toolvisuals.client.music.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.music.MusicClient;

final class MusicPaint {
    private MusicPaint() {}
    static Component text(String key,Object... values) { return UiI18n.tr("toolvisuals.music."+key,values); }
    static void label(GuiGraphicsExtractor g,String text,float x,float y,float width,float height,float size,int color) {
        UiPaint.baseline(g,UiPaint.fit(g,Component.literal(text),width,size),x,UiPaint.textBaseline(g,y,height,size),size,color,1);
    }
    static void cover(GuiGraphicsExtractor g,String key,float x,float y,float size,UiPalette palette) {
        UiPaint.rounded(g,x,y,size,size,Math.min(9,size/6),palette.selected);
        UiPaint.icon(g,UiPaint.Icon.MUSIC,x+size*.28f,y+size*.28f,size*.44f,palette.accentText,1);
        MusicClient.drawArtwork(key,g,x+3,y+3,size-6);
    }
    static void progress(GuiGraphicsExtractor g,float x,float y,float width,double progress,boolean enabled,UiPalette palette) {
        UiPaint.rounded(g,x,y,width,3,1.5f,UiPaint.alpha(palette.track,.6f));
        float fill=width*(float)Math.clamp(progress,0,1);
        if (fill>0) UiPaint.rounded(g,x,y,fill,3,1.5f,UiPaint.alpha(palette.accent,enabled?1:.55f));
    }
    static void control(GuiGraphicsExtractor g,UiPaint.Icon icon,float x,float y,float size,boolean active,boolean selected,boolean hover,UiPalette palette) {
        if (selected || hover) UiPaint.rounded(g,x,y,size,size,size/2,selected?palette.selected:palette.hover);
        UiPaint.icon(g,icon,x+size*.24f,y+size*.24f,size*.52f,selected?palette.accentText:palette.text,active?1:.25f);
    }
}

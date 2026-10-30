package ru.he3hauka.toolvisuals.client.session.ui;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.layout.*;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;
import ru.he3hauka.toolvisuals.client.session.*;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class SessionStatsHud {
    private static final String[] KEYS={"time","deaths","distance","blocks"};
    private static final SessionCounter.Snapshot PREVIEW=new SessionCounter.Snapshot(4832,2,421,1785);
    private static long lastSecond=-1,lastDeaths=-1,lastBlocks=-1,lastDistance=-1;
    private static String[] values={"00:00:00","0","0","0"},labels=new String[4];
    private static UiLanguage language;
    private static String heading;
    private SessionStatsHud(){}
    public static void draw(GuiGraphicsExtractor g,UiPalette palette){
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;var snapshot=HudPlacement.samples()?PREVIEW:SessionStats.snapshot();
        if(language!=config.language()){language=config.language();heading=UiI18n.tr("toolvisuals.session.title").getString();for(int i=0;i<KEYS.length;i++)labels[i]=UiI18n.tr("toolvisuals.session."+KEYS[i]).getString();}
        long distance=(long)snapshot.distance();
        if(lastSecond!=snapshot.seconds()||lastDeaths!=snapshot.deaths()||lastBlocks!=snapshot.blocks()||lastDistance!=distance){
            lastSecond=snapshot.seconds();lastDeaths=snapshot.deaths();lastBlocks=snapshot.blocks();lastDistance=distance;
            long seconds=snapshot.seconds();values=new String[]{String.format(Locale.ROOT,"%02d:%02d:%02d",seconds/3600,seconds/60%60,seconds%60),Long.toString(snapshot.deaths()),Long.toString(distance),Long.toString(snapshot.blocks())};
        }
        float scale=Math.clamp(client.getWindow().getHeight()/720f,1,2)*config.number(SESSION_SCALE)/client.getWindow().getGuiScale();
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        float w=144,h=110;for(int i=0;i<KEYS.length;i++)w=Math.max(w,UiPaint.textWidth(g,labels[i],.75f)+UiPaint.textWidth(g,values[i],.75f)+28);
        float x=HudFormats.position(g.guiWidth()/scale,w,HudPlacement.value(SESSION_X)),y=HudFormats.position(g.guiHeight()/scale,h,HudPlacement.value(SESSION_Y));
        HudPlacement.place(g,HudElement.SESSION,x,y,w,h);
        UiPaint.rounded(g,x,y,w,h,7,palette.hudPanel);UiPaint.icon(g,UiPaint.Icon.HUD,x+8,y+5,14,palette.accentText,1);
        UiPaint.baseline(g,heading,x+28,UiPaint.textBaseline(g,y,24,.84f),.84f,palette.text,1);
        for(int i=0;i<KEYS.length;i++){float row=y+24+i*21,baseline=UiPaint.textBaseline(g,row,21,.75f);UiPaint.baseline(g,labels[i],x+8,baseline,.75f,palette.muted,1);UiPaint.baseline(g,values[i],x+w-8-UiPaint.textWidth(g,values[i],.75f),baseline,.75f,palette.text,1);}
        g.pose().popMatrix();g.pose().popMatrix();
    }
}

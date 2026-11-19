package ru.he3hauka.toolvisuals.client.optimization.network.ui;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;
import ru.he3hauka.toolvisuals.client.optimization.network.NetworkCounter;
import ru.he3hauka.toolvisuals.client.optimization.network.NetworkOptimizations;
import ru.he3hauka.toolvisuals.client.optimization.network.NetworkTelemetry;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class KryptonHud {
    private static final String[] KEYS={"sent","vanilla","saving","copies","frames"};
    private static final NetworkCounter.Snapshot PREVIEW=new NetworkCounter.Snapshot(680,39204,39204,38192);
    private static final String[] labels=new String[KEYS.length],values=new String[KEYS.length];
    private static UiLanguage language;
    private static long refreshed;
    private static String footer;
    private static NetworkCounter.Snapshot snapshot=NetworkCounter.EMPTY;
    private KryptonHud() {}

    public static void draw(GuiGraphicsExtractor g,UiPalette palette){
        var config=ToolVisualsClient.CONFIG;
        if(!HudPlacement.preview()&&!config.flag(KRYPTON_STATISTICS))return;
        long now=System.nanoTime();
        if(language!=config.language()||now-refreshed>=250_000_000L||HudPlacement.samples()){
            language=config.language();refreshed=now;
            snapshot=HudPlacement.samples()?PREVIEW:NetworkTelemetry.snapshot();
            for(int i=0;i<KEYS.length;i++)labels[i]=text(KEYS[i]);
            values[0]=bytes(snapshot.sentBytes());values[1]=bytes(snapshot.vanillaBytes());values[2]="0 B · 0%";
            values[3]=bytes(snapshot.avoidedFrameCopyBytes());values[4]=Long.toString(snapshot.frames());
            footer=text(NetworkOptimizations.external()?"external":NetworkTelemetry.measuring()||HudPlacement.samples()?"scope":"waiting");
        }
        var client=Minecraft.getInstance();
        float scale=Math.clamp(client.getWindow().getHeight()/720f,1,2)*config.number(KRYPTON_SCALE)/client.getWindow().getGuiScale();
        float width=Math.max(260,UiPaint.textWidth(g,footer,.65f)+20),height=190;
        for(int i=0;i<labels.length;i++)width=Math.max(width,UiPaint.textWidth(g,labels[i],.75f)+UiPaint.textWidth(g,values[i],.75f)+32);
        float x=HudFormats.position(g.guiWidth()/scale,width,HudPlacement.value(KRYPTON_X)),y=HudFormats.position(g.guiHeight()/scale,height,HudPlacement.value(KRYPTON_Y));
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        HudPlacement.place(g,HudElement.KRYPTON,x,y,width,height);
        UiPaint.rounded(g,x,y,width,height,8,palette.hudPanel);
        UiPaint.icon(g,UiPaint.Icon.PING,x+9,y+6,14,palette.accentText,1);
        UiPaint.baseline(g,"KryptonOptimization",x+29,UiPaint.textBaseline(g,y,26,.84f),.84f,palette.text,1);
        for(int i=0;i<labels.length;i++){
            float row=y+28+i*21,baseline=UiPaint.textBaseline(g,row,21,.75f);
            UiPaint.baseline(g,labels[i],x+10,baseline,.75f,palette.muted,1);
            UiPaint.baseline(g,values[i],x+width-10-UiPaint.textWidth(g,values[i],.75f),baseline,.75f,i==3?palette.accentText:palette.text,1);
        }
        float maximum=Math.max(1,snapshot.vanillaBytes());
        bar(g,palette,x+10,y+139,width-20,snapshot.sentBytes()/maximum,palette.accent);
        bar(g,palette,x+10,y+151,width-20,snapshot.vanillaBytes()/maximum,palette.track);
        UiPaint.baseline(g,footer,x+10,UiPaint.textBaseline(g,y+166,18,.65f),.65f,palette.faint,1);
        g.pose().popMatrix();g.pose().popMatrix();
    }
    private static void bar(GuiGraphicsExtractor g,UiPalette palette,float x,float y,float width,float ratio,int color){
        UiPaint.rounded(g,x,y,width,6,3,palette.inset);
        UiPaint.rounded(g,x,y,width*Math.clamp(ratio,0,1),6,3,color);
    }
    private static String text(String key){return UiI18n.tr("toolvisuals.krypton."+key).getString();}
    private static String bytes(long value){
        if(value<1024)return value+" B";
        if(value<1024L*1024)return String.format(Locale.ROOT,"%.1f KiB",value/1024d);
        if(value<1024L*1024*1024)return String.format(Locale.ROOT,"%.2f MiB",value/(1024d*1024));
        return String.format(Locale.ROOT,"%.2f GiB",value/(1024d*1024*1024));
    }
}

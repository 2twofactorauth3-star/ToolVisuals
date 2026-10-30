package ru.he3hauka.toolvisuals.client.music.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.music.MusicClient;
import ru.he3hauka.toolvisuals.client.music.model.MediaAction;
import ru.he3hauka.toolvisuals.client.music.model.MediaSession;
import ru.he3hauka.toolvisuals.client.music.model.MediaSnapshot;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class MusicHud {
    private static final MediaSession PREVIEW=new MediaSession("1","Spotify","Midnight City","M83","Hurry Up, We’re Dreaming","preview","playing",72_000,0,244_000,0,244_000,1,false,0,4095);
    private static final MediaAction[] CONTROLS={MediaAction.PREVIOUS,MediaAction.TOGGLE,MediaAction.NEXT};
    private static MediaSnapshot displayed=MediaSnapshot.EMPTY;
    private static MusicHudLayout layout;
    private static long shownSecond=-1,totalSeconds=-1;
    private static String elapsed="0:00",duration="0:00";
    private MusicHud() {}

    public static void draw(GuiGraphicsExtractor g,UiPalette palette) {
        var client=Minecraft.getInstance(); var config=ToolVisualsClient.CONFIG;
        var snapshot=MusicClient.snapshot(); var session=snapshot.selected(); boolean preview=HudPlacement.preview();
        if (preview && (HudPlacement.samples() || session==null)) session=PREVIEW;
        if (session==null && config.flag(MUSIC_HIDE_IDLE) && MusicClient.status().equals("idle") && !preview && !(client.gui.screen() instanceof ChatScreen)) return;
        layout=new MusicHudLayout(config.number(MUSIC_WIDTH),config.flag(MUSIC_COVER),config.flag(MUSIC_ARTIST),config.flag(MUSIC_PROGRESS),config.flag(MUSIC_CONTROLS));
        displayed=snapshot;
        float scale=Math.clamp(client.getWindow().getHeight()/720f,1,2)*config.number(MUSIC_SCALE)/client.getWindow().getGuiScale();
        float w=layout.width(),h=layout.height();
        float x=HudFormats.position(g.guiWidth()/scale,w,HudPlacement.value(MUSIC_X)),y=HudFormats.position(g.guiHeight()/scale,h,HudPlacement.value(MUSIC_Y));
        g.pose().pushMatrix(); g.pose().scale(scale,scale); HudPlacement.place(g,HudElement.MUSIC,x,y,w,h);
        try {
            UiPaint.rounded(g,x,y,w,h,8,palette.hudPanel);
            boolean canMute=layout.controls()&&MediaAction.MUTE.available(session);
            float tx=x+layout.textX(),textWidth=w-layout.textX()-(canMute?52:28);
            if (layout.cover()) MusicPaint.cover(g,session==PREVIEW?"":snapshot.artworkKey(),x+9,y+8,layout.artist()?40:32,palette);
            if (session==null) {
                MusicPaint.label(g,"MusicHUD",tx,y+7,textWidth,18,.83f,palette.text);
                MusicPaint.label(g,MusicPaint.text(MusicClient.status()).getString(),tx,y+28,w-layout.textX()-10,18,.63f,palette.muted);
            } else {
                MusicPaint.label(g,session.source(),tx,y+4,textWidth,13,.57f,palette.accentText);
                MusicPaint.label(g,session.title().isEmpty()?MusicPaint.text("untitled").getString():session.title(),tx,y+18,textWidth,20,.84f,palette.text);
                if (layout.artist()) MusicPaint.label(g,session.artist(),tx,y+37,w-layout.textX()-10,14,.66f,palette.muted);
            }
            UiPaint.icon(g,UiPaint.Icon.MUSIC,x+w-24,y+8,14,palette.muted,1);
            if(canMute)MusicPaint.control(g,session.volume().muted()?UiPaint.Icon.SPEAKER_OFF:UiPaint.Icon.SPEAKER,x+w-50,y+3,24,true,session.volume().muted(),false,palette);
            long now=System.nanoTime();
            if (layout.progress()) MusicPaint.progress(g,x+10,y+layout.contentHeight()+2,w-20,session==null?0:session.progress(snapshot.received(),now),MediaAction.SEEK.available(session),palette);
            if (layout.controls()) {
                for (var action : CONTROLS) {
                    var icon=action==MediaAction.TOGGLE?(session!=null&&session.playing()?UiPaint.Icon.PAUSE:UiPaint.Icon.PLAY):action==MediaAction.PREVIOUS?UiPaint.Icon.PREVIOUS:UiPaint.Icon.NEXT;
                    MusicPaint.control(g,icon,x+layout.controlX(action),y+layout.controlsY(),24,action.available(session),action==MediaAction.TOGGLE,false,palette);
                }
                if (session!=null) {
                    long second=(session.positionAt(snapshot.received(),now)-session.start())/1000,total=(session.end()-session.start())/1000;
                    if (second!=shownSecond) { shownSecond=second; elapsed=MediaSession.time(second*1000); }
                    if (total!=totalSeconds) { totalSeconds=total; duration=MediaSession.time(total*1000); }
                    MusicPaint.label(g,elapsed,x+10,y+layout.controlsY()+2,55,20,.6f,palette.muted);
                    MusicPaint.label(g,duration,x+w-10-UiPaint.textWidth(g,duration,.6f),y+layout.controlsY()+2,55,20,.6f,palette.muted);
                }
            }
        } finally { g.pose().popMatrix(); g.pose().popMatrix(); }
    }
    public static boolean click(double mouseX,double mouseY,boolean twice) {
        var box=HudPlacement.bounds().get(HudElement.MUSIC);
        if (box==null || layout==null || !box.contains(mouseX,mouseY)) return false;
        double x=(mouseX-box.x)*layout.width()/box.width,y=(mouseY-box.y)*layout.height()/box.height;
        var session=displayed.selected();
        if(layout.controls()&&MediaAction.MUTE.available(session)&&x>=layout.width()-50&&x<layout.width()-26&&y<28) {
            MusicClient.command(displayed,session,MediaAction.MUTE,session.volume().muted()?0:1);return true;
        }
        if (x>=layout.width()-28 && y<28 || layout.cover() && x<54 && y<layout.contentHeight() || twice && y<layout.contentHeight()) {
            MusicClient.open(Minecraft.getInstance().gui.screen()); return true;
        }
        var action=layout.actionAt(x,y);
        if (action!=null) { if (action.available(session)) MusicClient.command(displayed,session,action,0); return true; }
        if (layout.progressAt(x,y)) { if (MediaAction.SEEK.available(session)) MusicClient.seek(displayed,session,(x-10)/(layout.width()-20)); return true; }
        return false;
    }
}

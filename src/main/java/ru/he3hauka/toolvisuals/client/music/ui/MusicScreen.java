package ru.he3hauka.toolvisuals.client.music.ui;

import java.util.EnumMap;
import java.util.Map;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.music.MusicClient;
import ru.he3hauka.toolvisuals.client.music.model.MediaAction;
import ru.he3hauka.toolvisuals.client.music.model.MediaSession;
import ru.he3hauka.toolvisuals.client.music.model.MediaSnapshot;
import ru.he3hauka.toolvisuals.client.music.model.MediaAdjustment;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.MUSIC_HUD;

public final class MusicScreen extends VisualEditorScreen {
    private static final int SEEK_X=208,SEEK_Y=171,SEEK_W=446;
    private static final int VOLUME_X=252,VOLUME_Y=285,VOLUME_W=344,SOURCES_Y=322;
    private final Map<MediaAction,MediaButton> transport=new EnumMap<>(MediaAction.class);
    private final VisualsButton[] sources=new VisualsButton[3];
    private VisualsButton enabled,automatic,speed,previousPage,nextPage,backward,forward;
    private MediaSnapshot displayed=MediaSnapshot.EMPTY;
    private MediaAdjustment adjustment;
    private int first;
    private long shownSecond=-1,totalSeconds=-1;
    private String elapsed="0:00",duration="0:00";
    private double shownRate=Double.NaN;

    public MusicScreen(Screen parent) { super(parent,MusicPaint.text("title")); }
    @Override protected void init() {
        frame(680,532); transport.clear(); adjustment=null; shownRate=Double.NaN;
        enabled=button(432,12,224,28,MusicPaint.text("enable"),UiPaint.Icon.MUSIC,
                ()->ToolVisualsClient.CONFIG.setActive(MUSIC_HUD,!ToolVisualsClient.CONFIG.active(MUSIC_HUD)),()->ToolVisualsClient.CONFIG.active(MUSIC_HUD));
        control(264,216,32,MediaAction.SHUFFLE,()->UiPaint.Icon.SHUFFLE,()->session()!=null&&session().shuffle());
        control(308,216,32,MediaAction.PREVIOUS,()->UiPaint.Icon.PREVIOUS,()->false);
        control(354,208,48,MediaAction.TOGGLE,()->session()!=null&&session().playing()?UiPaint.Icon.PAUSE:UiPaint.Icon.PLAY,()->true);
        control(416,216,32,MediaAction.NEXT,()->UiPaint.Icon.NEXT,()->false);
        control(460,216,32,MediaAction.REPEAT,()->UiPaint.Icon.REPEAT,()->session()!=null&&session().repeat()!=0);
        control(504,216,32,MediaAction.STOP,()->UiPaint.Icon.STOP,()->false);
        control(208,270,30,MediaAction.MUTE,()->session()!=null&&session().volume().muted()?UiPaint.Icon.SPEAKER_OFF:UiPaint.Icon.SPEAKER,()->session()!=null&&session().volume().muted());
        speed=button(568,217,86,30,Component.literal("1×"),null,()->{
            var session=session(); if(session!=null) MusicClient.command(displayed,session,MediaAction.RATE,nextRate(session.rate()));
        },null);
        speed.setTooltip(Tooltip.create(MusicPaint.text("rate")));
        backward=button(208,211,42,20,Component.literal("−10s"),null,()->seekBy(-10_000),null);
        forward=button(208,236,42,20,Component.literal("+10s"),null,()->seekBy(10_000),null);
        automatic=button(404,SOURCES_Y+8,188,26,MusicPaint.text("automatic"),null,()->MusicClient.select(displayed,null),()->displayed.pinned().isEmpty());
        previousPage=button(600,SOURCES_Y+8,24,26,Component.empty(),UiPaint.Icon.BACK,()->first=Math.max(0,first-3),null);
        nextPage=button(630,SOURCES_Y+8,24,26,Component.empty(),UiPaint.Icon.ARROW,()->first+=3,null);
        for(int row=0;row<sources.length;row++) {
            final int index=row;
            sources[row]=button(26,SOURCES_Y+44+row*29,628,25,Component.empty(),null,()->{
                int position=first+index; if(position<displayed.sessions().size()) MusicClient.select(displayed,displayed.sessions().get(position));
            },()->first+index<displayed.sessions().size() && displayed.sessions().get(first+index).id().equals(displayed.current()));
        }
        button(26,SOURCES_Y+164,132,28,MusicPaint.text("retry"),UiPaint.Icon.RESET,MusicClient::retry,null);
        button(522,SOURCES_Y+164,132,28,UiI18n.tr("toolvisuals.ui.back"),UiPaint.Icon.BACK,this::onClose,null);
    }
    private void control(int x,int y,int size,MediaAction action,java.util.function.Supplier<UiPaint.Icon> icon,java.util.function.BooleanSupplier selected) {
        var button=addWidget(new MediaButton(px+x,py+y,size,MusicPaint.text(action.wire),icon,()->palette,()->{
            var session=session();MusicClient.command(displayed,session,action,action==MediaAction.MUTE&&session!=null&&!session.volume().muted()?1:0);
        },selected));
        controls.add(button);transport.put(action,button);
    }
    private MediaSession session() { return displayed.selected(); }
    private static double nextRate(double current) {
        for(double speed:new double[]{.5,.75,1,1.25,1.5,1.75,2}) if(speed>current+.01) return speed;
        return .5;
    }
    private void seekBy(long offset) {
        var session=session();
        if(MediaAction.SEEK.available(session) && session.end()>session.start())
            MusicClient.seek(displayed,session,(double)(session.positionAt(displayed.received(),System.nanoTime())+offset-session.start())/(session.end()-session.start()));
        else MusicClient.command(displayed,session,offset<0?MediaAction.REWIND:MediaAction.FORWARD,0);
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my) {
        displayed=MusicClient.snapshot();var session=session();long now=System.nanoTime();
        enabled.setMessage(MusicPaint.text(ToolVisualsClient.CONFIG.active(MUSIC_HUD)?"enabled":"enable"));
        for(var entry:transport.entrySet())entry.getValue().active=entry.getKey().available(session);
        var toggle=transport.get(MediaAction.TOGGLE);toggle.setMessage(MusicPaint.text(session!=null&&session.playing()?"pause":"play"));
        speed.active=MediaAction.RATE.available(session);
        double rate=session==null?1:session.rate();
        if(rate!=shownRate){shownRate=rate;speed.setMessage(Component.literal(java.math.BigDecimal.valueOf(rate).stripTrailingZeros().toPlainString()+"×"));}
        boolean canSeek=MediaAction.SEEK.available(session);
        backward.active=canSeek||MediaAction.REWIND.available(session);forward.active=canSeek||MediaAction.FORWARD.available(session);
        backward.setMessage(Component.literal(canSeek?"−10s":"«"));forward.setMessage(Component.literal(canSeek?"+10s":"»"));
        automatic.active=MusicClient.status().equals("ready")||MusicClient.status().equals("idle");
        if(adjustment!=null&&!adjustment.matches(displayed))adjustment=null;
        MusicPaint.cover(g,displayed.artworkKey(),px+26,py+72,154,palette);
        String source=session==null?"MusicHUD":session.source();
        MusicPaint.label(g,source,px+SEEK_X,py+66,SEEK_W,18,.7f,palette.accentText);
        String title=session==null?MusicPaint.text(MusicClient.status()).getString():session.title().isEmpty()?MusicPaint.text("untitled").getString():session.title();
        MusicPaint.label(g,title,px+SEEK_X,py+88,SEEK_W,29,1.12f,palette.text);
        if(session!=null) {
            MusicPaint.label(g,session.artist(),px+SEEK_X,py+121,SEEK_W,20,.84f,palette.muted);
            MusicPaint.label(g,session.album(),px+SEEK_X,py+144,SEEK_W,16,.65f,palette.faint);
        }
        boolean seeking=adjustment!=null&&adjustment.action()==MediaAction.SEEK;
        double progress=seeking?adjustment.fraction():session==null?0:session.progress(displayed.received(),now);
        MusicPaint.progress(g,px+SEEK_X,py+SEEK_Y,SEEK_W,progress,canSeek,palette);
        boolean hover=canSeek&&mx>=SEEK_X&&mx<=SEEK_X+SEEK_W&&my>=SEEK_Y-7&&my<=SEEK_Y+10;
        if(hover||seeking) UiPaint.rounded(g,px+SEEK_X+(float)progress*SEEK_W-4,py+SEEK_Y-3,8,9,4,palette.accentText);
        long position=session==null?0:seeking?(long)((session.end()-session.start())*adjustment.fraction()):session.positionAt(displayed.received(),now)-session.start();
        long total=session==null?0:session.end()-session.start();
        if(position/1000!=shownSecond){shownSecond=position/1000;elapsed=MediaSession.time(position);}
        if(total/1000!=totalSeconds){totalSeconds=total/1000;duration=MediaSession.time(total);}
        MusicPaint.label(g,elapsed,px+SEEK_X,py+184,100,16,.65f,palette.muted);
        MusicPaint.label(g,duration,px+SEEK_X+SEEK_W-UiPaint.textWidth(g,duration,.65f),py+184,100,16,.65f,palette.muted);
        if(session!=null && session.repeat()==1)MusicPaint.label(g,"1",px+478,py+212,10,12,.5f,palette.accentText);
        boolean canVolume=MediaAction.VOLUME.available(session),adjustingVolume=adjustment!=null&&adjustment.action()==MediaAction.VOLUME;
        double level=adjustingVolume?adjustment.fraction():canVolume?session.volume().level():0;
        String volumeLabel=MusicPaint.text(canVolume?"volume."+session.volume().scope():"volume.unavailable").getString();
        MusicPaint.label(g,volumeLabel,px+VOLUME_X,py+265,VOLUME_W,16,.62f,palette.muted);
        MusicPaint.progress(g,px+VOLUME_X,py+VOLUME_Y,VOLUME_W,level,canVolume,palette);
        boolean volumeHover=canVolume&&mx>=VOLUME_X&&mx<=VOLUME_X+VOLUME_W&&my>=VOLUME_Y-7&&my<=VOLUME_Y+10;
        if(volumeHover||adjustingVolume)UiPaint.rounded(g,px+VOLUME_X+(float)level*VOLUME_W-4,py+VOLUME_Y-3,8,9,4,palette.accentText);
        MusicPaint.label(g,canVolume?Math.round(level*100)+"%":"—",px+610,py+270,44,28,.8f,canVolume?palette.text:palette.faint);
        MusicPaint.label(g,MusicPaint.text(canVolume?"volume.hint."+session.volume().scope():"volume.hint.unavailable").getString(),px+SEEK_X,py+297,SEEK_W,15,.55f,palette.faint);
        UiPaint.rounded(g,px+20,py+SOURCES_Y,640,136,8,palette.field);
        MusicPaint.label(g,MusicPaint.text("sources").getString(),px+28,py+SOURCES_Y+8,356,26,.79f,palette.text);
        first=Math.clamp(first,0,Math.max(0,displayed.sessions().size()-3));
        previousPage.active=first>0;nextPage.active=first+3<displayed.sessions().size();
        for(int row=0;row<sources.length;row++) {
            var button=sources[row];int positionInList=first+row;button.visible=positionInList<displayed.sessions().size();
            if(button.visible) {
                var item=displayed.sessions().get(positionInList);
                button.setMessage(Component.literal(item.source()+" · "+(item.title().isEmpty()?MusicPaint.text("untitled").getString():item.title())));
            }
        }
        if(displayed.sessions().isEmpty())MusicPaint.label(g,MusicPaint.text("no_sources").getString(),px+28,py+SOURCES_Y+61,610,32,.75f,palette.muted);
        String feedback=MusicClient.feedback();
        MusicPaint.label(g,MusicPaint.text(feedback.isEmpty()?"hint":"result."+feedback).getString(),px+26,py+SOURCES_Y+140,628,19,.64f,feedback.isEmpty()?palette.faint:palette.accentText);
        if(!hover && mx>=26 && mx<=180 && my>=72 && my<=226)
            MusicPaint.label(g,MusicPaint.text("local").getString(),px+26,py+238,154,20,.61f,palette.faint);
    }
    @Override public boolean mouseClicked(MouseButtonEvent raw,boolean twice) {
        var event=adjusted(raw);double x=event.x()-px,y=event.y()-py;
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT && x>=SEEK_X&&x<=SEEK_X+SEEK_W&&y>=SEEK_Y-7&&y<=SEEK_Y+10 && MediaAction.SEEK.available(session())) {
            adjustment=new MediaAdjustment(displayed,session(),MediaAction.SEEK,(x-SEEK_X)/SEEK_W);return true;
        }
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT && x>=VOLUME_X&&x<=VOLUME_X+VOLUME_W&&y>=VOLUME_Y-7&&y<=VOLUME_Y+10 && MediaAction.VOLUME.available(session())) {
            adjustment=new MediaAdjustment(displayed,session(),MediaAction.VOLUME,(x-VOLUME_X)/VOLUME_W);return true;
        }
        return super.mouseClicked(raw,twice);
    }
    @Override public boolean mouseDragged(MouseButtonEvent raw,double dx,double dy) {
        if(adjustment!=null && raw.button()==InputConstants.MOUSE_BUTTON_LEFT) {
            boolean seeking=adjustment.action()==MediaAction.SEEK;
            adjustment=adjustment.move((adjusted(raw).x()-px-(seeking?SEEK_X:VOLUME_X))/(seeking?SEEK_W:VOLUME_W));return true;
        }
        return super.mouseDragged(raw,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent raw) {
        if(adjustment!=null && raw.button()==InputConstants.MOUSE_BUTTON_LEFT) {
            if(adjustment.matches(MusicClient.snapshot())) {
                if(adjustment.action()==MediaAction.SEEK)MusicClient.seek(adjustment.snapshot(),adjustment.session(),adjustment.fraction());
                else MusicClient.command(adjustment.snapshot(),adjustment.session(),MediaAction.VOLUME,adjustment.fraction());
            }
            adjustment=null;return true;
        }
        return super.mouseReleased(raw);
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical) {
        double localX=x/scale-px,localY=y/scale-py;
        if(localX>=20&&localX<=660&&localY>=SOURCES_Y&&localY<=SOURCES_Y+136) {
            first=Math.clamp(first-(int)Math.signum(vertical),0,Math.max(0,displayed.sessions().size()-3));return true;
        }
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
}

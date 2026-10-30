package ru.he3hauka.toolvisuals.client.cosmetic.accessory.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryClient;
import ru.he3hauka.toolvisuals.client.cosmetic.preview.PlayerPreview;
import ru.he3hauka.toolvisuals.client.cape.CapeClient;
import ru.he3hauka.toolvisuals.client.cape.CapeTexture;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation.*;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.*;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.cosmetic.accessory.*;
import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.COSMETICS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class CosmeticsScreen extends VisualEditorScreen {
    private static final int PAGE_SIZE=6,PRESET_PAGE_SIZE=4;
    private AccessoryLook.Back back;
    private AccessoryLook.Head head;
    private ColorPalette backColors,headColors;
    private float backScale,headScale,speed,yaw=180;
    private boolean animated,rotating,armor;
    private MotionPreview.Action movement=MotionPreview.Action.STILL;
    private final CosmeticMotion previewMotion=new CosmeticMotion();
    private static final java.util.UUID PREVIEW_ACTOR=new java.util.UUID(0,77);
    private long previewTick=Long.MIN_VALUE;
    private float previousYaw=180;
    private double previewTurn;
    private AccessoryLook cached;
    private final long opened=System.nanoTime();
    private VisualsButton save;
    private int backPage,headPage,presetPage;
    private final CapeTexture capePreview=new CapeTexture(true);

    public CosmeticsScreen(Screen parent){super(parent,text("title"));load(ToolVisualsClient.CONFIG.accessories());}
    private static Component text(String key,Object... args){return UiI18n.tr("toolvisuals.cosmetics."+key,args);}
    private void load(AccessoryLook look){back=look.back();head=look.head();backColors=look.backColors();headColors=look.headColors();backScale=look.backScale();headScale=look.headScale();speed=look.speed();animated=look.animated();cached=look;backPage=back.ordinal()/PAGE_SIZE;headPage=head.ordinal()/PAGE_SIZE;}
    private AccessoryLook draft(){if(cached==null)cached=new AccessoryLook(back,head,backColors,headColors,backScale,headScale,speed,animated);return cached;}

    @Override protected void init(){
        frame(850,612);
        catalog(false);catalog(true);
        button(20,316,146,32,text("back_color"),UiPaint.Icon.RENDER,()->color(false),null);
        button(174,316,146,32,text("head_color"),UiPaint.Icon.RENDER,()->color(true),null);
        slider(20,364,300,text("back_size"),.65,1.35,.05,()->backScale,value->{backScale=(float)value;cached=null;},value->Math.round(value*100)+"%");
        slider(20,404,300,text("head_size"),.65,1.35,.05,()->headScale,value->{headScale=(float)value;cached=null;},value->Math.round(value*100)+"%");
        slider(20,444,300,text("speed"),.5,2,.1,()->speed,value->{speed=(float)value;cached=null;},value->String.format(java.util.Locale.ROOT,"%.1fx",value));
        button(20,488,300,28,text("animated"),null,()->{animated=!animated;cached=null;},()->animated);
        button(700,64,128,28,text("armor"),null,()->armor=!armor,()->armor);
        for(var next:MotionPreview.Action.values())button(350+(next.ordinal()%4)*120,445+(next.ordinal()/4)*32,112,28,text("movement_"+next.name().toLowerCase(java.util.Locale.ROOT)),null,()->movement=next,()->movement==next);
        var presets=AccessoryPresets.Preset.values();
        pages(728,506,presetPage,presets.length,PRESET_PAGE_SIZE,page->presetPage=page);
        for(int i=presetPage*PRESET_PAGE_SIZE;i<Math.min(presets.length,(presetPage+1)*PRESET_PAGE_SIZE);i++){
            var preset=presets[i];
            button(350+(i%PRESET_PAGE_SIZE)*120,534,112,24,text("preset_"+preset.name().toLowerCase(java.util.Locale.ROOT)),null,()->{load(AccessoryPresets.create(preset));yaw=180;rebuildWidgets();},null);
        }
        button(20,568,136,28,UiI18n.tr("toolvisuals.editor.cancel"),null,this::onClose,null);
        save=button(166,568,154,28,UiI18n.tr("toolvisuals.editor.apply"),UiPaint.Icon.CHECK,()->{ToolVisualsClient.CONFIG.setAccessories(draft());onClose();},null);
        button(350,568,140,28,text("clear"),UiPaint.Icon.RESET,()->{load(AccessoryLook.EMPTY);rebuildWidgets();},null);
        button(498,568,160,28,UiI18n.tr("toolvisuals.outfits.title"),UiPaint.Icon.STAR,()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.cosmetic.outfit.ui.OutfitScreen(this,this::draft,this::load)),null);
        button(666,568,162,28,UiI18n.tr("toolvisuals.capes.title"),UiPaint.Icon.RENDER,()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.cape.ui.CapeEditorScreen(this)),null);
    }
    private void catalog(boolean headSlot){
        Enum<?>[] entries=headSlot?AccessoryLook.Head.values():AccessoryLook.Back.values();
        int page=headSlot?headPage:backPage,y=headSlot?206:82;
        pages(220,y-28,page,entries.length,PAGE_SIZE,next->{if(headSlot)headPage=next;else backPage=next;});
        for(int i=page*PAGE_SIZE;i<Math.min(entries.length,(page+1)*PAGE_SIZE);i++){
            var entry=entries[i];int index=i%PAGE_SIZE;
            button(20+(index%2)*154,y+(index/2)*32,146,28,text((headSlot?"head_":"back_")+entry.name().toLowerCase(java.util.Locale.ROOT)),null,()->{
                if(headSlot)head=(AccessoryLook.Head)entry;else back=(AccessoryLook.Back)entry;
                cached=null;yaw=headSlot?0:180;
            },()->entry==(headSlot?head:back));
        }
    }
    private void pages(int x,int y,int page,int count,int size,java.util.function.IntConsumer change){
        button(x,y,26,24,Component.literal("‹"),null,()->{change.accept(page-1);rebuildWidgets();},null).active=page>0;
        button(x+64,y,26,24,Component.literal("›"),null,()->{change.accept(page+1);rebuildWidgets();},null).active=(page+1)*size<count;
    }
    private void pageLabel(GuiGraphicsExtractor g,int x,int y,int page,int count,int size){
        var label=Component.literal((page+1)+"/"+((count+size-1)/size));
        float width=UiPaint.textWidth(g,label.getString(),.7f);
        label(g,label,x+45-Math.round(width/2),y,40,24,.7f);
    }
    private void color(boolean headSlot){
        minecraft.gui.setScreen(new ColorEditorScreen(this,text(headSlot?"head_color":"back_color"),ColorStyle.from(headSlot?headColors:backColors),style->{if(headSlot)headColors=style;else backColors=style;cached=null;},true));
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        UiPaint.text(g,text("back"),px+20,py+60,.8f,palette.muted,1);
        UiPaint.text(g,text("head"),px+20,py+184,.8f,palette.muted,1);
        pageLabel(g,220,54,backPage,AccessoryLook.Back.values().length,PAGE_SIZE);
        pageLabel(g,220,178,headPage,AccessoryLook.Head.values().length,PAGE_SIZE);
        label(g,text("presets"),350,506,300,24,.73f);
        pageLabel(g,728,506,presetPage,AccessoryPresets.Preset.values().length,PRESET_PAGE_SIZE);
        UiPaint.rounded(g,px+350,py+102,478,330,9,palette.field);
        UiPaint.rounded(g,px+356,py+414,466,12,6,UiPaint.alpha(palette.accent,.06f));
        preview(g);
        label(g,text("drag_hint"),350,64,338,28,.73f);
        boolean changed=!draft().equals(ToolVisualsClient.CONFIG.accessories());
        String key=!ToolVisualsClient.CONFIG.active(COSMETICS)?"disabled":changed?"unsaved":AccessoryClient.shared()?"shared":AccessoryClient.available()?"syncing":"local";
        label(g,text(key),350,430,478,14,.66f);save.active=true;
    }
    private void label(GuiGraphicsExtractor g,Component text,int x,int y,int w,int h,float size){UiPaint.baseline(g,UiPaint.fit(g,text,w,size),px+x,UiPaint.textBaseline(g,py+y,h,size),size,palette.muted,1);}
    private void preview(GuiGraphicsExtractor g){
        if(minecraft.player==null){label(g,text("join_world"),380,220,418,64,.85f);return;}
        var state=PlayerPreview.prepare(minecraft.player,armor,false);state.bodyRot=yaw;
        var cape=CapeClient.design(minecraft.player.getUUID());if(cape!=null)capePreview.apply(state,cape);
        double seconds=(System.nanoTime()-opened)/1e9,ticks=seconds*20;long tick=(long)Math.floor(ticks);
        if(previewTick!=tick){previewTurn=previewTick==Long.MIN_VALUE?0:(yaw-previousYaw)/Math.max(1,tick-previewTick);previousYaw=yaw;previewTick=tick;}
        var config=ToolVisualsClient.CONFIG;
        var motion=previewMotion.sample(PREVIEW_ACTOR,PREVIEW_ACTOR,tick,(float)(ticks-tick),draft(),MotionPreview.input(movement,tick,previewTurn),config.flag(COSMETIC_MOTION)?config.value(COSMETIC_MOTION_STRENGTH):0,config.flag(COSMETIC_HEAD_SWAY));
        state.ageInTicks=(float)(ticks%12000);state.walkAnimationPos=(float)(motion.stride*Math.TAU/.6662);state.walkAnimationSpeed=(float)(motion.move*(1-motion.glide)*(1-motion.swim));
        if(movement==MotionPreview.Action.GLIDE){state.isFallFlying=true;state.fallFlyingTimeInTicks=10;}
        if(movement==MotionPreview.Action.SWIM){state.swimAmount=1;state.isVisuallySwimming=true;state.pose=Pose.SWIMMING;}
        if(movement==MotionPreview.Action.CROUCH){state.isCrouching=true;state.pose=Pose.CROUCHING;}
        float jump=MotionPreview.jumpHeight(movement,ticks);
        ((AccessoryVisualState)state).toolvisuals$accessories(new AccessoryFrame(draft(),seconds,0,false,true,config.flag(COSMETIC_HIDE_CAPE),motion));
        boolean tall=switch(head){case HORNS,ANTLERS,WITCH_HAT,ASTROLABE->true;default->false;};
        float previewScale=tall?92:104;
        float center=tall?1.14f:1.02f;
        PlayerPreview.draw(g,state,px+356,py+108,466,306,previewScale,center-jump*.20f);
    }
    @Override public void removed(){capePreview.close();super.removed();}
    @Override public boolean mouseClicked(MouseButtonEvent raw,boolean twice){
        var e=adjusted(raw);double x=e.x()-px,y=e.y()-py;
        if(e.button()==InputConstants.MOUSE_BUTTON_LEFT&&x>=350&&x<828&&y>=102&&y<432){rotating=true;setFocused(null);return true;}
        return super.mouseClicked(raw,twice);
    }
    @Override public boolean mouseDragged(MouseButtonEvent raw,double dx,double dy){if(rotating){yaw+=(float)(dx/scale*.7);return true;}return super.mouseDragged(raw,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent event){rotating=false;return super.mouseReleased(event);}
}

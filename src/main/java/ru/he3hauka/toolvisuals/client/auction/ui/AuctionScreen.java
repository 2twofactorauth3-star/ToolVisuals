package ru.he3hauka.toolvisuals.client.auction.ui;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.auction.*;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public final class AuctionScreen extends Screen {
    private final Screen parent;
    private final AuctionStore store = ToolVisualsClient.AUCTIONS;
    private final List<VisualsButton> controls = new ArrayList<>();
    private final List<FormWidget> form = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private final List<ProfileRow> profiles = new ArrayList<>();
    private final List<String> commands = new ArrayList<>();
    private final Motion scroll = new Motion(0), profileScroll = new Motion(0);
    private UiViewport viewport;
    private UiPalette palette = new UiPalette(0);
    private AuctionPlan original;
    private List<AuctionPlan.Step> manual = List.of();
    private AuctionPlan.Mode mode = AuctionPlan.Mode.COMMANDS;
    private String draftId="", draftServer="", minimum="300", maximum="300", success="", failure="", pending="";
    private Component feedback = Component.empty();
    private boolean dirty, failed, draggingForm, draggingProfiles;
    private int px,py,pw,ph,leftX,leftWidth,rightX,rightWidth,top,bottom,contentHeight;
    private float scrollTarget, profileTarget, scrollGrab;
    private long lastFrame;
    private VisualsButton delete, save, record, run, power;
    public AuctionScreen(Screen parent) {
        super(Component.literal("AuctionHelper")); this.parent=parent;
        var active=store.active(AuctionHelper.server());
        read(active);
    }
    private static Component tr(String key,Object... args) {return UiI18n.tr("toolvisuals.auction."+key,args);}
    private void read(AuctionPlan plan) {
        original=plan; String address=AuctionHelper.server();
        draftId=plan==null?address:plan.id; draftServer=plan==null?address:plan.server;
        if(draftId.isEmpty())draftId="Profile "+(store.plans().size()+1);
        mode=plan==null?AuctionPlan.Mode.COMMANDS:plan.mode;
        minimum=plan==null?"300":Integer.toString(plan.minSeconds); maximum=plan==null?"300":Integer.toString(plan.maxSeconds);
        success=plan==null?"":plan.successText; failure=plan==null?"":plan.failureText;
        manual=plan==null?List.of():plan.manual; commands.clear(); if(plan!=null)commands.addAll(plan.commands); if(commands.isEmpty())commands.add("");
        dirty=false; pending=""; scrollTarget=0; scroll.snap(0);
    }
    private static final class FormWidget {
        public final net.minecraft.client.gui.components.AbstractWidget widget;
        public final int x,y;
        FormWidget(net.minecraft.client.gui.components.AbstractWidget widget,int x,int y){this.widget=widget;this.x=x;this.y=y;}
    }
    private static final class Label {
        public final Component text; public final int x,y,width; public final float scale;
        Label(Component text,int x,int y,int width,float scale){this.text=text;this.x=x;this.y=y;this.width=width;this.scale=scale;}
    }
    @Override protected void init() {
        controls.clear();form.clear();labels.clear();profiles.clear();
        viewport=new UiViewport(width,height,minecraft.getWindow().getGuiScale());
        pw=Math.min(840,viewport.width-32);ph=Math.min(528,viewport.height-32);px=(viewport.width-pw)/2;py=(viewport.height-ph)/2;
        leftX=px+18;leftWidth=pw<700?164:202;rightX=leftX+leftWidth+18;rightWidth=px+pw-18-rightX;
        top=py+90;bottom=py+ph-88;
        power=control(px+pw-128,py+12,84,28,"enable",CHIP,null,()->ToolVisualsClient.CONFIG.setActive(VisualModule.AUCTION_HELPER,!ToolVisualsClient.CONFIG.active(VisualModule.AUCTION_HELPER)),()->ToolVisualsClient.CONFIG.active(VisualModule.AUCTION_HELPER));
        control(px+pw-38,py+12,26,28,"back",ICON,UiPaint.Icon.BACK,this::onClose,null);
        control(leftX,py+ph-73,leftWidth/2-3,30,"new",CHIP,UiPaint.Icon.PLUS,()->select(null),null);
        delete=control(leftX+leftWidth/2+3,py+ph-73,leftWidth/2-3,30,"delete",CHIP,null,this::delete,null);
        int w=(rightWidth-12)/3;
        save=control(rightX,py+ph-73,w,30,"save",CHIP,null,this::save,null);
        record=control(rightX+w+6,py+ph-73,w,30,"record",CHIP,null,this::record,null);
        run=control(rightX+(w+6)*2,py+ph-73,w,30,"run",CHIP,null,this::run,null);
        int y=0;
        label("id",0,y,rightWidth); field(0,y+19,rightWidth,draftId,128,value->draftId=value,false); y+=60;
        label("server",0,y,rightWidth); field(0,y+19,rightWidth,draftServer,255,value->draftServer=value,false); y+=60;
        int half=(rightWidth-10)/2;
        label("minimum",0,y,half); label("maximum",half+10,y,half);
        field(0,y+19,half,minimum,5,value->minimum=value,true);field(half+10,y+19,half,maximum,5,value->maximum=value,true); y+=60;
        label("mode",0,y,rightWidth);
        int modeWidth=(rightWidth-8)/3;
        for(var option:AuctionPlan.Mode.values()) formButton(option.ordinal()*(modeWidth+4),y+20,modeWidth,30,"mode."+option.name().toLowerCase(Locale.ROOT),CHIP,null,
                ()->{mode=option;changed();},()->mode==option);
        y+=62;
        int responsesY=y+22+commands.size()*40+34+32;
        label("success_text",0,responsesY,rightWidth);field(0,responsesY+19,rightWidth,success,512,value->success=value,false);
        label("failure_text",0,responsesY+60,rightWidth);field(0,responsesY+79,rightWidth,failure,512,value->failure=value,false);
        label("commands",0,y,rightWidth);y+=22;
        for(int i=0;i<commands.size();i++) {
            int index=i;field(0,y,rightWidth-30,commands.get(i),256,value->commands.set(index,value),false);
            formButton(rightWidth-26,y,26,32,"remove_command",ICON,UiPaint.Icon.CLOSE,()->{commands.remove(index);if(commands.isEmpty())commands.add("");changed();rebuildWidgets();},null);y+=40;
        }
        var add=formButton(0,y,rightWidth,28,"add_command",CHIP,UiPaint.Icon.PLUS,()->{if(commands.size()<AuctionPlan.MAX_COMMANDS){commands.add("");changed();rebuildWidgets();}},null);
        add.active=commands.size()<AuctionPlan.MAX_COMMANDS; y+=34;
        labels.add(new Label(tr("commands_hint"),0,y,rightWidth,.72f));y+=32+120;
        label("manual_title",0,y,rightWidth-80);
        formButton(rightWidth-76,y-3,76,26,"clear_manual",GHOST,null,()->{manual=List.of();changed();rebuildWidgets();},null).active=!manual.isEmpty();y+=26;
        labels.add(new Label(tr("manual_count",Math.max(0,manual.size()-1)),0,y,rightWidth,.8f));y+=28;
        for(var step:manual) {
            Component description=step.command.isEmpty()?tr("step_click",step.slot+1,step.menu.title,step.item.name):Component.literal("/"+step.command);
            labels.add(new Label(description,0,y,rightWidth,.75f));y+=23;
        }
        labels.add(new Label(tr("record_finish_hint"),0,y,rightWidth,.72f));y+=28;
        labels.add(new Label(tr("result_hint"),0,y,rightWidth,.72f));y+=30;
        contentHeight=y; refreshProfiles(); scrollTarget=Math.clamp(scrollTarget,0,maxScroll());scroll.snap(scrollTarget);position();updateActions();lastFrame=System.nanoTime();
    }
    private void changed(){dirty=true;pending="";}
    private void label(String key,int x,int y,int width){labels.add(new Label(tr(key),x,y,width,.8f));}
    private EditBox field(int x,int y,int w,String value,int max,Consumer<String> responder,boolean number) {
        var field=new EditBox(UiFont.get(),0,0,w-18,32,Component.empty()) {
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
                UiPaint.rounded(g,getX()-9,getY(),getWidth()+18,32,5,palette.field);
                boolean valid=!number||digits(getValue());setTextColor(valid?palette.text:0xffff879c);g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(32));
                super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
            }
        };
        field.setBordered(false);field.setTextShadow(false);field.setMaxLength(max);
        field.setValue(value);field.setResponder(text->{responder.accept(text);changed();});addWidget(field);form.add(new FormWidget(field,x+9,y));return field;
    }
    private VisualsButton formButton(int x,int y,int w,int h,String key,VisualsButton.Kind kind,UiPaint.Icon icon,Runnable action,java.util.function.BooleanSupplier selected) {
        var button=addWidget(new VisualsButton(0,0,w,h,tr(key),kind,icon,Component.empty(),action,selected));form.add(new FormWidget(button,x,y));return button;
    }
    private VisualsButton control(int x,int y,int w,int h,String key,VisualsButton.Kind kind,UiPaint.Icon icon,Runnable action,java.util.function.BooleanSupplier selected) {
        var button=addWidget(new VisualsButton(x,y,w,h,tr(key),kind,icon,Component.empty(),action,selected));controls.add(button);return button;
    }
    private void refreshProfiles() {
        profiles.forEach(this::removeWidget);profiles.clear();
        for(var plan:store.plans())profiles.add(addWidget(new ProfileRow(plan)));
        profileTarget=Math.clamp(profileTarget,0,maxProfiles());profileScroll.snap(profileTarget);
    }
    private void updateActions() {
        power.setMessage(tr(ToolVisualsClient.CONFIG.active(VisualModule.AUCTION_HELPER)?"disable":"enable"));
        delete.active=original!=null;delete.setMessage(tr(pending.equals("delete")?"confirm_delete":"delete"));
        record.setMessage(tr(AuctionHelper.recording()?"cancel_record":"record"));
        record.active=AuctionHelper.recording()||!AuctionHelper.server().isEmpty();
        run.active=ToolVisualsClient.CONFIG.active(VisualModule.AUCTION_HELPER)&&!AuctionHelper.recording()&&!AuctionHelper.server().isEmpty();
    }
    private AuctionPlan draft() {
        return new AuctionPlan(draftId,draftServer,mode,Integer.parseInt(minimum),Integer.parseInt(maximum),
                commands.stream().filter(value->!value.isBlank()).toList(),manual,success,failure);
    }
    private static boolean digits(String value){if(value.isEmpty())return false;for(int i=0;i<value.length();i++)if(value.charAt(i)<'0'||value.charAt(i)>'9')return false;return true;}
    private boolean save() {
        try {
            var plan=draft();var existing=store.get(plan.id);
            if(existing!=null&&original!=existing){feedback("duplicate_id",true);return false;}
            store.put(plan,true);original=plan;dirty=false;pending="";feedback("saved",false,plan.id);refreshProfiles();position();return true;
        }catch(IOException error){feedback("io_error",true);}catch(RuntimeException error){feedback("bad_settings",true);}return false;
    }
    private void record() {
        if(AuctionHelper.recording()){AuctionHelper.cancelRecording("record_cancelled");return;}
        if(!save())return;
        AuctionHelper.startRecording(original);
        if(AuctionHelper.recording()){KeyMapping.releaseAll();minecraft.gui.setScreen(null);}
    }
    private void run() {
        if(!save())return;
        if(!original.server.equals(AuctionHelper.server())){feedback("wrong_server",true);return;}
        if(!original.ready()){feedback("bad_settings",true);return;}
        if(AuctionHelper.runNow()){KeyMapping.releaseAll();minecraft.gui.setScreen(null);}else feedback("enable_first",true);
    }
    private void select(AuctionPlan plan) {
        String action="select:"+(plan==null?"":plan.id);
        if(dirty&&!pending.equals(action)){pending=action;feedback("discard_hint",true);return;}
        read(plan);feedback=Component.empty();rebuildWidgets();
    }
    private void delete() {
        if(original==null)return;
        if(!pending.equals("delete")){pending="delete";feedback("confirm_hint",false);return;}
        try {store.delete(original.id);read(null);feedback("deleted",false);rebuildWidgets();}catch(IOException error){feedback("io_error",true);}
    }
    private void feedback(String key,boolean failed,Object... args){feedback=tr(key,args);this.failed=failed;}
    private float maxScroll(){return Math.max(0,contentHeight-(bottom-top));}
    private float maxProfiles(){return Math.max(0,profiles.size()*56-4-(bottom-top));}
    private void position(){
        for(var entry:form){var widget=entry.widget;widget.setX(rightX+entry.x);widget.setY(top+entry.y-Math.round(scroll.value()));widget.visible=widget.getY()<bottom&&widget.getY()+widget.getHeight()>top;}
        for(int i=0;i<profiles.size();i++){var row=profiles.get(i);row.setY(top+i*56-Math.round(profileScroll.value()));row.visible=row.getY()<bottom&&row.getY()+52>top;}
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){if(minecraft.level==null)extractPanorama(g,partial);g.fill(0,0,width,height,0x88070910);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        if(width!=g.guiWidth()||height!=g.guiHeight()){resize(g.guiWidth(),g.guiHeight());return;}
        long now=System.nanoTime();float dt=Math.clamp((now-lastFrame)/1e9f,0,1);lastFrame=now;
        scroll.step(scrollTarget,dt,22);profileScroll.step(profileTarget,dt,22);position();updateActions();palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);
        mx=Math.round(mx/viewport.scale);my=Math.round(my/viewport.scale);g.pose().pushMatrix();g.pose().scale(viewport.scale,viewport.scale);
        UiPaint.shadow(g,px,py,pw,ph,12,1);UiPaint.rounded(g,px-1,py-1,pw+2,ph+2,13,palette.border);UiPaint.rounded(g,px,py,pw,ph,12,palette.panel);
        line(g,getTitle(),px+18,py+9,pw-168,30,1.1f,palette.text);
        Component status=tr("status."+AuctionHelper.status());
        if(AuctionHelper.recording())status=tr("status.recording",AuctionHelper.recordedClicks());
        else if(AuctionHelper.remainingSeconds()>0)status=tr("countdown",status,AuctionHelper.remainingSeconds());
        var active=store.active(AuctionHelper.server());if(active!=null)status=tr("active_status",active.id,status);
        line(g,status,px+18,py+45,pw-36,24,.8f,palette.accentText);
        line(g,tr("profiles",store.plans().size()),leftX,py+71,leftWidth,18,.75f,palette.muted);
        line(g,tr(dirty?"unsaved":"settings"),rightX,py+71,rightWidth,18,.75f,palette.muted);
        g.fill(rightX-10,top,rightX-9,bottom,palette.border);
        g.enableScissor(leftX,top,leftX+leftWidth,bottom);
        for(var row:profiles)row.extractRenderState(g,mx,my>=top&&my<bottom?my:-1000,partial);
        if(profiles.isEmpty())line(g,tr("no_profiles"),leftX+8,top+8,leftWidth-16,24,.8f,palette.muted);g.disableScissor();
        g.enableScissor(rightX,top,rightX+rightWidth,bottom);
        for(var label:labels)line(g,label.text,rightX+label.x,top+label.y-scroll.value(),label.width,18,label.scale,palette.muted);
        for(var entry:form){if(entry.widget instanceof VisualsButton b)b.frame(palette,1,dt);entry.widget.extractRenderState(g,mx,my>=top&&my<bottom?my:-1000,partial);}g.disableScissor();
        bar(g,rightX+rightWidth-2,scroll.value(),maxScroll());bar(g,leftX+leftWidth-2,profileScroll.value(),maxProfiles());
        for(var button:controls){button.frame(palette,1,dt);button.extractRenderState(g,mx,my,partial);}
        line(g,feedback,px+18,py+ph-34,pw-36,22,.75f,failed?0xffff879c:palette.accentText);g.pose().popMatrix();
    }
    private void bar(GuiGraphicsExtractor g,int x,float value,float max){if(max<=0)return;float height=bottom-top,thumb=Math.max(18,height*height/(height+max));UiPaint.rounded(g,x,top,2,height,1,palette.track);UiPaint.rounded(g,x,top+(height-thumb)*value/max,2,thumb,1,palette.accent);}
    private void line(GuiGraphicsExtractor g,Component text,float x,float y,float width,float height,float scale,int color){UiPaint.baseline(g,UiPaint.fit(g,text,width,scale),x,UiPaint.textBaseline(g,y,height,scale),scale,color,1);}
    private final class ProfileRow extends Button {
        public final AuctionPlan plan;
        ProfileRow(AuctionPlan plan){super(leftX,0,leftWidth-6,52,Component.literal(plan.id),b->select(plan),DEFAULT_NARRATION);this.plan=plan;}
        @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial){
            boolean chosen=original!=null&&original.id.equalsIgnoreCase(plan.id);UiPaint.rounded(g,getX(),getY(),getWidth(),52,7,chosen||isHoveredOrFocused()?palette.hover:palette.raised);
            line(g,Component.literal(plan.id),getX()+9,getY()+3,getWidth()-18,25,.9f,palette.text);
            line(g,Component.literal(plan.server),getX()+9,getY()+30,getWidth()-18,18,.7f,palette.muted);
        }
    }
    private MouseButtonEvent adjusted(MouseButtonEvent e){return new MouseButtonEvent(e.x()/viewport.scale,e.y()/viewport.scale,e.buttonInfo());}
    @Override public boolean mouseClicked(MouseButtonEvent raw,boolean twice){
        var e=adjusted(raw);
        if(e.button()==InputConstants.MOUSE_BUTTON_LEFT&&e.y()>=top&&e.y()<bottom){
            if(maxScroll()>0&&e.x()>=rightX+rightWidth-7&&e.x()<rightX+rightWidth+4){draggingForm=true;beginDrag(e.y(),scroll.value(),maxScroll());return true;}
            if(maxProfiles()>0&&e.x()>=leftX+leftWidth-7&&e.x()<leftX+leftWidth+4){draggingProfiles=true;beginDrag(e.y(),profileScroll.value(),maxProfiles());return true;}
        }
        if(e.y()<top||e.y()>=bottom){form.forEach(entry->entry.widget.visible=false);profiles.forEach(row->row.visible=false);}
        boolean result=super.mouseClicked(e,twice);position();if(getFocused()!=null&&!children().contains(getFocused()))setFocused(null);return result;
    }
    private void beginDrag(double y,float current,float max){float h=bottom-top,thumb=Math.max(18,h*h/(h+max)),start=top+(h-thumb)*current/max;scrollGrab=y>=start&&y<start+thumb?(float)y-start:thumb/2;dragScroll(y);}
    private void dragScroll(double y){float max=draggingForm?maxScroll():maxProfiles(),h=bottom-top,thumb=Math.max(18,h*h/(h+max));float target=Math.clamp((float)(y-top-scrollGrab)/Math.max(1,h-thumb)*max,0,max);if(draggingForm){scrollTarget=target;scroll.snap(target);}else{profileTarget=target;profileScroll.snap(target);}position();}
    @Override public void mouseMoved(double x,double y){super.mouseMoved(x/viewport.scale,y/viewport.scale);}
    @Override public boolean mouseReleased(MouseButtonEvent e){draggingForm=draggingProfiles=false;return super.mouseReleased(adjusted(e));}
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(draggingForm||draggingProfiles){dragScroll(adjusted(e).y());return true;}return super.mouseDragged(adjusted(e),dx/viewport.scale,dy/viewport.scale);}
    @Override public boolean mouseScrolled(double x,double y,double h,double v){if(y/viewport.scale>=top&&y/viewport.scale<bottom){if(x/viewport.scale>=rightX)scrollTarget=Math.clamp(scrollTarget-(float)v*44,0,maxScroll());else profileTarget=Math.clamp(profileTarget-(float)v*44,0,maxProfiles());return true;}return false;}
    @Override public boolean keyPressed(KeyEvent e){if(!(getFocused() instanceof EditBox)&&(e.key()==InputConstants.KEY_PAGEDOWN||e.key()==InputConstants.KEY_PAGEUP)){scrollTarget=Math.clamp(scrollTarget+(e.key()==InputConstants.KEY_PAGEUP?-1:1)*(bottom-top),0,maxScroll());return true;}return super.keyPressed(e);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){if(dirty&&!pending.equals("close")){pending="close";feedback("discard_hint",true);return;}minecraft.gui.setScreen(parent);}
}

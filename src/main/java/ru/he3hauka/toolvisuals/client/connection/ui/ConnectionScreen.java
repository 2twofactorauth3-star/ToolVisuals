package ru.he3hauka.toolvisuals.client.connection.ui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.connection.ConnectionIdentity;
import ru.he3hauka.toolvisuals.client.connection.ConnectionVersions;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public final class ConnectionScreen extends Screen {
    private final Screen parent;
    private final boolean focusVersions;
    private final List<VisualsButton> controls=new ArrayList<>(), versions=new ArrayList<>();
    private final Motion scroll=new Motion(0);
    private UiViewport viewport;
    private UiPalette palette;
    private EditBox nickname,address;
    private VisualsButton save,join;
    private int px,py,pw,ph,listY,listBottom,columns,rowWidth;
    private float scrollTarget;
    private String draftName,draftAddress;
    private long lastFrame;
    public ConnectionScreen(Screen parent,boolean focusVersions) {
        super(tr("title")); this.parent=parent; this.focusVersions=focusVersions;
    }
    private static Component tr(String key,Object... args) { return UiI18n.tr("toolvisuals.connection."+key,args); }
    @Override protected void init() {
        controls.clear(); versions.clear();
        viewport=new UiViewport(width,height,minecraft.getWindow().getGuiScale());
        pw=Math.min(560,viewport.width-32); ph=Math.min(484,viewport.height-32); px=(viewport.width-pw)/2; py=(viewport.height-ph)/2;
        boolean shortWindow=ph<400;
        int nickY=shortWindow?78:82;
        nickname=addWidget(field(px+30,py+nickY,pw-112,32,tr("nickname")));
        nickname.setMaxLength(ConnectionIdentity.MAX_NAME_LENGTH); nickname.setValue(draftName==null?minecraft.getUser().getName():draftName);
        nickname.setResponder(value -> { draftName=value; updateActions(); });
        add(px+pw-56,py+nickY,28,32,tr("restore"),ICON,UiPaint.Icon.RESET,() -> {
            ConnectionIdentity.restore(minecraft); nickname.setValue(minecraft.getUser().getName());
        });
        int addressY=ph-(shortWindow?92:100);
        address=addWidget(field(px+30,py+addressY,pw-60,32,tr("address")));
        address.setMaxLength(255); address.setValue(draftAddress==null?minecraft.options.lastMpIp:draftAddress);
        address.setResponder(value -> { draftAddress=value; updateActions(); });
        add(px+pw-40,py+12,26,26,UiI18n.tr("toolvisuals.ui.back"),ICON,UiPaint.Icon.BACK,this::onClose);
        save=add(px+20,py+ph-48,(pw-48)/2,30,tr("apply"),CHIP,null,() -> apply(false));
        join=add(px+28+(pw-48)/2,py+ph-48,(pw-48)/2,30,tr("join"),CHIP,null,() -> apply(true));
        listY=py+(shortWindow?145:155); listBottom=py+ph-(shortWindow?106:138);
        columns=pw>=400?2:1; rowWidth=(pw-48-(columns-1)*8)/columns;
        var choices=ConnectionVersions.choices();
        for(int i=0;i<choices.size();i++) {
            var choice=choices.get(i);
            var b=addWidget(new VisualsButton(0,0,rowWidth,30,Component.literal(choice.label),MENU,null,Component.empty(),
                    () -> ConnectionVersions.select(choice),() -> ConnectionVersions.selected(choice)));
            versions.add(b);
            if(focusVersions&&draftName==null&&ConnectionVersions.selected(choice)) scrollTarget=i/columns*32;
        }
        scrollTarget=Math.clamp(scrollTarget,0,maxScroll()); scroll.snap(scrollTarget); positionVersions(); updateActions();
        if(focusVersions) {
            for(int i=0;i<choices.size();i++) if(ConnectionVersions.selected(choices.get(i))) { setFocused(versions.get(i)); break; }
        } else { setFocused(nickname); nickname.setFocused(true); nickname.moveCursorToEnd(false); }
        lastFrame=System.nanoTime();
    }
    private EditBox field(int x,int y,int width,int height,Component label) {
        var field=new EditBox(UiFont.get(),x,y,width,height,label) {
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
                if(getValue().isEmpty()&&!isFocused()) {
                    UiPaint.baseline(g,UiPaint.fit(g,label,getWidth(),1),getX(),
                            UiPaint.textBaseline(g,getY(),getHeight(),1),1,palette.muted,1);return;
                }
                g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(getHeight()));
                super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
            }
        };
        field.setBordered(false);field.setTextShadow(false);return field;
    }
    private VisualsButton add(int x,int y,int w,int h,Component label,VisualsButton.Kind kind,UiPaint.Icon icon,Runnable action) {
        var b=addWidget(new VisualsButton(x,y,w,h,label,kind,icon,Component.empty(),action,null));controls.add(b);return b;
    }
    private void updateActions() {
        if(save==null||join==null||nickname==null||address==null)return;
        save.active=ConnectionIdentity.valid(nickname.getValue());
        nickname.setTooltip(save.active?null:net.minecraft.client.gui.components.Tooltip.create(tr("invalid_nickname")));
        join.active=save.active&&ServerAddress.isValidAddress(address.getValue().strip());
    }
    private void apply(boolean connect) {
        if(!ConnectionIdentity.valid(nickname.getValue()))return;
        String server=address.getValue().strip();
        if(connect&&!ServerAddress.isValidAddress(server))return;
        try{ConnectionIdentity.apply(minecraft,nickname.getValue());}
        catch(java.io.IOException error){nickname.setTooltip(net.minecraft.client.gui.components.Tooltip.create(UiI18n.tr("toolvisuals.accounts.io_error")));return;}
        if(ServerAddress.isValidAddress(server)) {minecraft.options.lastMpIp=server;minecraft.options.save();}
        if(connect) ConnectScreen.startConnecting(parent,minecraft,ServerAddress.parseString(server),
                new ServerData(server,server,ServerData.Type.OTHER),false,null);
        else onClose();
    }
    private float maxScroll() {return Math.max(0,((versions.size()+columns-1)/columns)*32-2-(listBottom-listY));}
    private void positionVersions() {
        for(int i=0;i<versions.size();i++) {
            var b=versions.get(i);b.setX(px+20+i%columns*(rowWidth+8));b.setY(listY+i/columns*32-Math.round(scroll.value()));
            b.visible=b.getY()<listBottom&&b.getY()+30>listY;
        }
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial) {
        extractPanorama(g,partial);g.fill(0,0,width,height,0x80070910);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        if(width!=g.guiWidth()||height!=g.guiHeight()){resize(g.guiWidth(),g.guiHeight());return;}
        long now=System.nanoTime();float dt=Math.clamp((now-lastFrame)/1e9f,0,1);lastFrame=now;
        scroll.step(scrollTarget,dt,22);positionVersions();palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);
        mx=Math.round(mx/viewport.scale);my=Math.round(my/viewport.scale);
        g.pose().pushMatrix();g.pose().scale(viewport.scale,viewport.scale);
        UiPaint.shadow(g,px,py,pw,ph,12,1);UiPaint.rounded(g,px-1,py-1,pw+2,ph+2,13,palette.border);
        UiPaint.rounded(g,px,py,pw,ph,12,palette.panel);
        UiPaint.icon(g,UiPaint.Icon.PLAYER,px+20,py+18,15,palette.accentText,1);
        UiPaint.baseline(g,getTitle().getString(),px+44,UiPaint.textBaseline(g,py+12,26,1),1,palette.text,1);
        g.fill(px+20,py+49,px+pw-20,py+50,palette.border);
        boolean small=ph<400; int nickY=py+(small?78:82);
        line(g,tr("nickname"),px+20,py+56,pw-40,.85f,palette.muted);
        UiPaint.rounded(g,px+20,nickY,pw-84,32,6,palette.field);
        nickname.setTextColor(palette.text);nickname.extractRenderState(g,mx,my,partial);
        line(g,tr("version_value",ConnectionVersions.label()),px+20,py+(small?120:129),pw-40,.9f,palette.text);
        g.enableScissor(px+20,listY,px+pw-20,listBottom);
        for(var b:versions){b.frame(palette,1,dt);b.extractRenderState(g,mx,my>=listY&&my<listBottom?my:-1000,partial);}
        g.disableScissor();
        if(maxScroll()>0){float track=listBottom-listY,thumb=Math.max(18,track*track/(track+maxScroll()));
            UiPaint.rounded(g,px+pw-24,listY,2,track,1,palette.track);
            UiPaint.rounded(g,px+pw-24,listY+(track-thumb)*scroll.value()/maxScroll(),2,thumb,1,palette.accent);}
        if(!small)line(g,tr("address"),px+20,py+ph-124,pw-40,.8f,palette.muted);
        UiPaint.rounded(g,px+20,address.getY(),pw-40,32,6,palette.field);
        address.setTextColor(palette.text);address.extractRenderState(g,mx,my,partial);
        for(var b:controls){b.frame(palette,1,dt);b.extractRenderState(g,mx,my,partial);}
        g.pose().popMatrix();
    }
    private void line(GuiGraphicsExtractor g,Component text,int x,int y,int width,float scale,int color) {
        UiPaint.baseline(g,UiPaint.fit(g,text,width,scale),x,UiPaint.textBaseline(g,y,20,scale),scale,color,1);
    }
    private MouseButtonEvent adjusted(MouseButtonEvent e){return new MouseButtonEvent(e.x()/viewport.scale,e.y()/viewport.scale,e.buttonInfo());}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        var event=adjusted(e);if(event.y()<listY||event.y()>=listBottom)versions.forEach(b->b.visible=false);
        boolean result=super.mouseClicked(event,twice);positionVersions();return result;
    }
    @Override public void mouseMoved(double x,double y){super.mouseMoved(x/viewport.scale,y/viewport.scale);}
    @Override public boolean mouseReleased(MouseButtonEvent e){return super.mouseReleased(adjusted(e));}
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){return super.mouseDragged(adjusted(e),dx/viewport.scale,dy/viewport.scale);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        double yy=y/viewport.scale;
        if(yy>=listY&&yy<listBottom){scrollTarget=Math.clamp(scrollTarget-(float)vertical*40,0,maxScroll());return true;}return false;
    }
    @Override public boolean keyPressed(KeyEvent e){
        if(e.key()==InputConstants.KEY_RETURN){if(e.hasControlDown()&&join.active)apply(true);else if(nickname.isFocused()&&save.active)apply(false);else return super.keyPressed(e);return true;}
        if(e.key()==InputConstants.KEY_PAGEUP||e.key()==InputConstants.KEY_PAGEDOWN){scrollTarget=Math.clamp(scrollTarget+(e.key()==InputConstants.KEY_PAGEUP?-1:1)*(listBottom-listY),0,maxScroll());return true;}
        return super.keyPressed(e);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}

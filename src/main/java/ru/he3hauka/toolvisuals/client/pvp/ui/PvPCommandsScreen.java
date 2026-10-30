package ru.he3hauka.toolvisuals.client.pvp.ui;

import java.util.*;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.pvp.CombatCommands;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public final class PvPCommandsScreen extends Screen {
    private static final int PITCH = 40;
    private final Screen parent;
    private final List<VisualsButton> controls = new ArrayList<>();
    private final List<Entry> rows = new ArrayList<>();
    private final Motion scroll = new Motion(0);
    private UiViewport viewport;
    private UiPalette palette = new UiPalette(0);
    private EditBox command;
    private VisualsButton add;
    private String draft = "";
    private int px,py,pw,ph,listY,listBottom;
    private float scrollTarget,elapsed;
    private long lastFrame;
    private static final class Entry {
        public final String command;
        public final VisualsButton label,remove;
        Entry(String command,VisualsButton label,VisualsButton remove) { this.command=command;this.label=label;this.remove=remove; }
    }
    public PvPCommandsScreen(Screen parent) { super(tr("title"));this.parent=parent; }
    private static Component tr(String key,Object... args) { return UiI18n.tr("toolvisuals.pvp_commands."+key,args); }
    @Override protected void init() {
        controls.clear();rows.clear();viewport=new UiViewport(width,height,minecraft.getWindow().getGuiScale());
        pw=Math.min(680,viewport.width-32);ph=Math.min(480,viewport.height-32);px=(viewport.width-pw)/2;py=(viewport.height-ph)/2;
        control(px+pw-40,py+12,26,26,UiI18n.tr("toolvisuals.ui.back"),ICON,UiPaint.Icon.BACK,this::onClose);
        command=addWidget(new EditBox(UiFont.get(),px+30,py+88,pw-170,32,tr("input")) {
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
                if(getValue().isEmpty()) {
                    line(g,tr("input"),getX(),getY(),getWidth(),getHeight(),.9f,palette.muted);
                    if(!isFocused())return;
                }
                g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(getHeight()));
                super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
            }
        });
        command.setBordered(false);command.setTextShadow(false);command.setMaxLength(CombatCommands.MAX_LENGTH);command.setValue(draft);
        command.setResponder(value->{draft=value;updateActions();});
        add=control(px+pw-126,py+88,106,32,tr("add"),CHIP,null,this::addCommand);
        control(px+20,py+ph-48,pw-40,30,tr("defaults"),GHOST,UiPaint.Icon.RESET,()->{
            ToolVisualsClient.CONFIG.setPvpBlockedCommands(CombatCommands.DEFAULTS);refresh();
        });
        listY=py+136;listBottom=py+ph-72;
        refresh();lastFrame=System.nanoTime();setFocused(command);command.setFocused(true);
    }
    private VisualsButton control(int x,int y,int w,int h,Component text,VisualsButton.Kind kind,UiPaint.Icon icon,Runnable action) {
        var button=addWidget(new VisualsButton(x,y,w,h,text,kind,icon,text,action,null));controls.add(button);return button;
    }
    private String typedRule() { try { return CombatCommands.rule(draft); }catch(IllegalArgumentException error) { return null; } }
    private void updateActions() {
        if(add==null)return;
        String rule=typedRule();var rules=ToolVisualsClient.CONFIG.pvpBlockedCommands();
        add.active=rule!=null&&!rules.contains(rule)&&rules.size()<CombatCommands.LIMIT;
    }
    private void addCommand() {
        String rule=typedRule();if(rule==null||!add.active)return;
        var next=new ArrayList<>(ToolVisualsClient.CONFIG.pvpBlockedCommands());next.add(rule);
        ToolVisualsClient.CONFIG.setPvpBlockedCommands(next);command.setValue("");refresh();
    }
    private void removeCommand(String rule) {
        var next=new ArrayList<>(ToolVisualsClient.CONFIG.pvpBlockedCommands());next.remove(rule);
        ToolVisualsClient.CONFIG.setPvpBlockedCommands(next);command.setValue("/"+rule);refresh();
    }
    private void refresh() {
        for(var row:rows) { removeWidget(row.label);removeWidget(row.remove); }rows.clear();
        for(String rule:ToolVisualsClient.CONFIG.pvpBlockedCommands()) {
            var label=addWidget(new VisualsButton(0,0,pw-82,36,Component.literal("/"+rule),MENU,null,tr("edit_hint"),()->command.setValue("/"+rule),null));
            var remove=addWidget(new VisualsButton(0,0,30,30,tr("remove"),ICON,UiPaint.Icon.CLOSE,tr("remove"),()->removeCommand(rule),null));
            rows.add(new Entry(rule,label,remove));
        }
        if(getFocused()!=null&&!children().contains(getFocused()))setFocused(null);
        scrollTarget=Math.clamp(scrollTarget,0,maxScroll());scroll.snap(scrollTarget);positionRows();updateActions();
    }
    private float maxScroll() { return Math.max(0,rows.size()*PITCH-(listBottom-listY)); }
    private void positionRows() {
        for(int i=0;i<rows.size();i++) {
            var row=rows.get(i);int y=listY+i*PITCH-Math.round(scroll.value());
            row.label.setX(px+20);row.label.setY(y);row.remove.setX(px+pw-56);row.remove.setY(y+3);
            row.label.visible=row.remove.visible=y<listBottom&&y+36>listY;
        }
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial) {
        if(minecraft.level==null)extractPanorama(g,partial);g.fill(0,0,width,height,0x80070910);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        if(width!=g.guiWidth()||height!=g.guiHeight()) { resize(g.guiWidth(),g.guiHeight());return; }
        long now=System.nanoTime();elapsed=Math.clamp((now-lastFrame)/1e9f,0,1);lastFrame=now;
        scroll.step(scrollTarget,elapsed,22);positionRows();palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);
        mx=Math.round(mx/viewport.scale);my=Math.round(my/viewport.scale);g.pose().pushMatrix();g.pose().scale(viewport.scale,viewport.scale);
        UiPaint.shadow(g,px,py,pw,ph,12,1);UiPaint.rounded(g,px-1,py-1,pw+2,ph+2,13,palette.border);UiPaint.rounded(g,px,py,pw,ph,12,palette.panel);
        line(g,getTitle(),px+20,py+12,pw-72,26,1,palette.text);g.fill(px+20,py+49,px+pw-20,py+50,palette.border);
        line(g,tr("hint"),px+20,py+56,pw-40,24,.83f,palette.muted);
        UiPaint.rounded(g,px+20,py+88,pw-154,32,6,palette.field);command.setTextColor(palette.text);command.extractRenderState(g,mx,my,partial);
        g.enableScissor(px+20,listY,px+pw-20,listBottom);
        for(var row:rows) {
            row.label.frame(palette,1,elapsed);row.remove.frame(palette,1,elapsed);
            row.label.extractRenderState(g,mx,my>=listY&&my<listBottom?my:-1000,partial);row.remove.extractRenderState(g,mx,my>=listY&&my<listBottom?my:-1000,partial);
        }
        g.disableScissor();
        if(rows.isEmpty())line(g,tr("empty"),px+30,listY+12,pw-60,40,.88f,palette.muted);
        if(maxScroll()>0) {
            float track=listBottom-listY,thumb=Math.max(18,track*track/(track+maxScroll()));
            UiPaint.rounded(g,px+pw-22,listY,2,track,1,palette.track);
            UiPaint.rounded(g,px+pw-22,listY+(track-thumb)*scroll.value()/maxScroll(),2,thumb,1,palette.accent);
        }
        for(var button:controls) { button.frame(palette,1,elapsed);button.extractRenderState(g,mx,my,partial); }
        g.pose().popMatrix();
    }
    private void line(GuiGraphicsExtractor g,Component text,int x,int y,int w,int h,float scale,int color) {
        UiPaint.baseline(g,UiPaint.fit(g,text,w,scale),x,UiPaint.textBaseline(g,y,h,scale),scale,color,1);
    }
    private MouseButtonEvent adjusted(MouseButtonEvent event) { return new MouseButtonEvent(event.x()/viewport.scale,event.y()/viewport.scale,event.buttonInfo()); }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice) {
        var actual=adjusted(event);
        if(actual.y()<listY||actual.y()>=listBottom)for(var row:rows) { row.label.visible=false;row.remove.visible=false; }
        boolean handled=super.mouseClicked(actual,twice);positionRows();return handled;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(adjusted(event)); }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) { return super.mouseDragged(adjusted(event),dx/viewport.scale,dy/viewport.scale); }
    @Override public void mouseMoved(double x,double y) { super.mouseMoved(x/viewport.scale,y/viewport.scale); }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical) {
        if(x/viewport.scale>=px+20&&x/viewport.scale<px+pw-20&&y/viewport.scale>=listY&&y/viewport.scale<listBottom) {
            scrollTarget=Math.clamp(scrollTarget-(float)vertical*40,0,maxScroll());return true;
        }
        return super.mouseScrolled(x/viewport.scale,y/viewport.scale,horizontal,vertical);
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if(event.key()==InputConstants.KEY_RETURN&&command.isFocused()&&add.active) { addCommand();return true; }
        return super.keyPressed(event);
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}

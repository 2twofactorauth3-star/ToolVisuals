package ru.he3hauka.toolvisuals.client.friends.ui;

import java.util.*;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.connection.ConnectionIdentity;
import ru.he3hauka.toolvisuals.client.friends.FriendStore;
import ru.he3hauka.toolvisuals.client.friends.FriendStore.ArmorStyle;
import ru.he3hauka.toolvisuals.client.friends.FriendStore.Option;
import ru.he3hauka.toolvisuals.client.friends.Friends;
import ru.he3hauka.toolvisuals.client.friends.FriendDisplay;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.color.HexColorField;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public final class FriendsScreen extends Screen {
    private final Screen parent;
    private final FriendStore store = ToolVisualsClient.FRIENDS;
    private final List<VisualsButton> controls = new ArrayList<>(), rows = new ArrayList<>(), options = new ArrayList<>();
    private final Motion scroll = new Motion(0),optionScroll=new Motion(0);
    private UiViewport viewport;
    private UiPalette palette = new UiPalette(0);
    private EditBox name;
    private HexColorField hex;
    private VisualsButton add, apply, remove,armorMode,inventoryMode;
    private final List<VisualsButton> personal=new ArrayList<>();
    private String selected = "", draft = "";
    private Component status = Component.empty();
    private int px, py, pw, ph, left, listY, listBottom, listWidth, right, rightWidth, colorY,optionsTop,optionsBottom;
    private float scrollTarget,optionTarget,elapsed;
    private long lastFrame;
    public FriendsScreen(Screen parent) { super(tr("title")); this.parent=parent; }
    private static Component tr(String key,Object... args) { return UiI18n.tr("toolvisuals.friends."+key,args); }
    @Override protected void init() {
        controls.clear();rows.clear();options.clear();personal.clear();viewport=new UiViewport(width,height,minecraft.getWindow().getGuiScale());
        pw=Math.min(800,viewport.width-28);ph=Math.min(492,viewport.height-28);px=(viewport.width-pw)/2;py=(viewport.height-ph)/2;
        left=px+18;listWidth=Math.round((pw-60)*.43f);right=left+listWidth+24;rightWidth=px+pw-18-right;
        listY=py+64;listBottom=py+ph-78;
        control(controls,px+pw-42,py+12,26,26,UiI18n.tr("toolvisuals.ui.back"),ICON,UiPaint.Icon.BACK,this::onClose,null);
        name=addWidget(field(left+8,py+ph-64,listWidth-46,30,tr("nickname")));name.setMaxLength(16);name.setValue(draft);
        name.setResponder(value->{draft=value;updateActions();});
        add=control(controls,left+listWidth-30,py+ph-64,30,30,tr("add"),ICON,UiPaint.Icon.PLUS,this::addFriend,null);
        colorY=py+ph-132;optionsTop=py+60;optionsBottom=colorY-28;
        for(var field:FriendDisplay.Field.values())personal.add(control(options,right,0,rightWidth-8,30,Component.empty(),OPTION,null,()->configureDisplay(field),()->{
            var friend=store.get(selected);if(friend==null)return false;
            return switch(field){case HEALTH->friend.display.health;case ARMOR->friend.display.armor;case INVENTORY->friend.display.inventory;};
        }));
        inventoryMode=control(options,right,0,rightWidth-8,30,Component.empty(),CHIP,UiPaint.Icon.HUD,this::nextInventoryMode,null);
        control(options,right,0,rightWidth-8,30,tr("armor"),OPTION,null,()->configure(Option.ARMOR),()->store.settings().armor);
        armorMode=control(options,right,0,rightWidth-8,30,Component.empty(),CHIP,UiPaint.Icon.RENDER,()->configure(Option.ARMOR_STYLE),null);
        control(options,right,0,rightWidth-8,30,tr("unarmored"),OPTION,null,()->configure(Option.UNARMORED),()->store.settings().unarmored);
        control(options,right,0,rightWidth-8,30,tr("name_color"),OPTION,null,()->configure(Option.NAME_COLOR),()->store.settings().nameColor);
        control(options,right,0,rightWidth-8,30,tr("friend_label"),OPTION,null,()->configure(Option.FRIEND_LABEL),()->store.settings().friendLabel);
        control(options,right,0,rightWidth-8,30,tr("global"),OPTION,null,()->configure(Option.GLOBAL),()->store.settings().global);
        control(options,right,0,rightWidth-8,30,tr("notify_join"),OPTION,null,()->configure(Option.JOIN),()->store.settings().join);
        control(options,right,0,rightWidth-8,30,tr("notify_leave"),OPTION,null,()->configure(Option.LEAVE),()->store.settings().leave);
        hex=addWidget(new HexColorField(right,colorY,rightWidth,30,tr("global_color"),"",this::nextColor));
        hex.setResponder(value->updateActions());
        apply=control(controls,right,colorY+36,rightWidth-42,28,tr("apply"),CHIP,null,this::applyColor,null);
        control(controls,right+rightWidth-36,colorY+36,36,28,UiI18n.tr("toolvisuals.editor.colors"),ICON,UiPaint.Icon.RENDER,this::nextColor,null);
        remove=control(controls,right,colorY+70,rightWidth,28,tr("remove"),GHOST,UiPaint.Icon.CLOSE,this::removeFriend,null);
        optionTarget=Math.clamp(optionTarget,0,maxScroll(options,33,optionsTop,optionsBottom));optionScroll.snap(optionTarget);
        refresh();lastFrame=System.nanoTime();setFocused(name);name.setFocused(true);
    }
    private EditBox field(int x,int y,int w,int h,Component hint) {
        var result=new EditBox(UiFont.get(),x,y,w,h,hint) {
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
                if(getValue().isEmpty()&&!isFocused()) { line(g,hint,getX(),getY(),getWidth(),getHeight(),palette.muted);return; }
                g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(getHeight()));
                super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
            }
        };
        result.setBordered(false);result.setTextShadow(false);return result;
    }
    private VisualsButton control(List<VisualsButton> group,int x,int y,int w,int h,Component text,VisualsButton.Kind kind,UiPaint.Icon icon,Runnable action,java.util.function.BooleanSupplier on) {
        var button=addWidget(new VisualsButton(x,y,w,h,text,kind,icon,text,action,on));group.add(button);return button;
    }
    private void refresh() {
        rows.forEach(this::removeWidget);rows.clear();
        if(store.get(selected)==null)selected=store.entries().isEmpty()?"":store.entries().getFirst().name;
        for(var friend:store.entries()) rows.add(addWidget(new VisualsButton(0,0,listWidth-5,42,
                Component.literal(friend.name),MENU,null,tr(Friends.online(friend.name)?"online":"offline"),
                ()->{selected=friend.name;refreshColor();updateActions();},()->friend.name.equalsIgnoreCase(selected)) {
            @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial) {
                super.extractContents(g,mx,my,partial);
                int color=store.color(friend);
                UiPaint.rounded(g,getX()+getWidth()-51,getY()+15,12,12,3,0xff000000|color);
            }
        }));
        scrollTarget=Math.clamp(scrollTarget,0,maxScroll());refreshColor();updateActions();positionRows();
    }
    private void refreshColor() {
        if(hex==null)return;
        var friend=store.get(selected);int color=store.settings().global||friend==null?store.settings().color:friend.color;
        hex.setMessage(tr(store.settings().global?"global_color":"personal_color",selected));
        hex.setValue(ru.he3hauka.toolvisuals.client.config.color.HexColor.format(color,true));
    }
    private Integer typedColor() {
        return hex==null?null:hex.color();
    }
    private void updateActions() {
        if(add==null||apply==null||remove==null||name==null)return;
        add.active=ConnectionIdentity.valid(name.getValue())&&store.get(name.getValue())==null&&store.entries().size()<FriendStore.LIMIT;
        apply.active=typedColor()!=null&&(store.settings().global||store.get(selected)!=null);remove.active=store.get(selected)!=null;
        armorMode.setMessage(tr("armor_mode",tr(store.settings().armorStyle==ArmorStyle.FILL?"fill":"overlay")));
        for(var field:FriendDisplay.Field.values()){
            var button=personal.get(field.ordinal());button.active=store.get(selected)!=null&&ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.FRIENDS_LIST);
            button.setMessage(tr("list_"+field.name().toLowerCase(Locale.ROOT),selected));
        }
        var profile=store.get(selected);inventoryMode.active=profile!=null&&ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.FRIENDS_LIST);
        inventoryMode.setMessage(tr("list_mode",selected,tr("list_mode_"+(profile==null?FriendDisplay.InventoryMode.DEFAULT:profile.display.mode).name().toLowerCase(Locale.ROOT))));
    }
    private void addFriend() {
        try { if(store.add(name.getValue())) { selected=name.getValue();name.setValue("");refresh(); } }
        catch(java.io.IOException|RuntimeException error) { status=tr("save_error"); }
    }
    private void removeFriend() {
        if(store.get(selected)==null)return;
        try { store.remove(selected);refresh(); } catch(java.io.IOException error) { status=tr("save_error"); }
    }
    private void configure(Option option) {
        try { store.configure(store.settings().toggle(option));refreshColor();updateActions(); }
        catch(java.io.IOException error) { status=tr("save_error"); }
    }
    private void configureDisplay(FriendDisplay.Field field){
        if(!ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.FRIENDS_LIST))return;
        var friend=store.get(selected);if(friend==null)return;
        try{store.display(selected,friend.display.toggle(field));updateActions();}
        catch(java.io.IOException error){ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors.report("Save friend HUD preferences",error);status=tr("save_error");}
    }
    private void nextInventoryMode(){
        if(!ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.FRIENDS_LIST))return;
        var friend=store.get(selected);if(friend==null)return;
        try{store.display(selected,friend.display.nextMode());updateActions();}
        catch(java.io.IOException error){ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors.report("Save friend inventory display mode",error);status=tr("save_error");}
    }
    private void applyColor() {
        var color=typedColor();if(color==null)return;
        try {
            var s=store.settings();
            if(s.global)store.configure(s.withStyle(ColorStyle.solid(color)));else if(store.get(selected)!=null)store.color(selected,color);
            status=tr("saved");refresh();
        } catch(java.io.IOException error) { status=tr("save_error"); }
    }
    private void nextColor() {
        var s=store.settings();var friend=store.get(selected);if(!s.global&&friend==null)return;
        var initial=s.global?s.style:friend.style;var typed=typedColor();
        if(typed!=null&&typed.intValue()!=initial.colors.getFirst())initial=ColorStyle.solid(typed);
        minecraft.gui.setScreen(new ColorEditorScreen(this,tr(s.global?"global_color":"personal_color",selected),initial,style->{
            try{var current=store.settings();if(current.global)store.configure(current.withStyle(style));else store.color(selected,style);status=tr("saved");}
            catch(java.io.IOException error){status=tr("save_error");}
        }));
    }
    private float maxScroll() { return maxScroll(rows,46,listY,listBottom); }
    private static float maxScroll(List<VisualsButton> buttons,int pitch,int top,int bottom) {
        return buttons.isEmpty()?0:Math.max(0,(buttons.size()-1)*pitch+buttons.getLast().getHeight()-(bottom-top));
    }
    private void positionRows() {
        positionRows(rows,left,listY,listBottom,46,scroll);
        positionRows(options,right,optionsTop,optionsBottom,33,optionScroll);
    }
    private static void positionRows(List<VisualsButton> buttons,int x,int top,int bottom,int pitch,Motion motion) {
        for(int i=0;i<buttons.size();i++) {
            var button=buttons.get(i);button.setX(x);button.setY(top+i*pitch-Math.round(motion.value()));
            button.visible=button.getY()<bottom&&button.getY()+button.getHeight()>top;
        }
    }
    private void drawRows(GuiGraphicsExtractor g,List<VisualsButton> buttons,int x,int top,int w,int bottom,int mx,int my,float partial) {
        g.enableScissor(x,top,x+w,bottom);
        for(var button:buttons){button.frame(palette,1,elapsed);button.extractRenderState(g,mx,my>=top&&my<bottom?my:-1000,partial);}
        g.disableScissor();
    }
    private void scrollbar(GuiGraphicsExtractor g,int x,int top,int bottom,Motion motion,float maximum) {
        if(maximum<=0)return;
        float track=bottom-top,thumb=Math.max(18,track*track/(track+maximum));
        UiPaint.rounded(g,x,top,2,track,1,palette.track);
        UiPaint.rounded(g,x,top+(track-thumb)*motion.value()/maximum,2,thumb,1,palette.accent);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial) {
        if(minecraft.level==null)extractPanorama(g,partial);g.fill(0,0,width,height,0x80070910);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        if(width!=g.guiWidth()||height!=g.guiHeight()) { resize(g.guiWidth(),g.guiHeight());return; }
        long now=System.nanoTime();elapsed=Math.clamp((now-lastFrame)/1e9f,0,1);lastFrame=now;
        scroll.step(scrollTarget,elapsed,22);optionScroll.step(optionTarget,elapsed,22);positionRows();
        palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);mx=Math.round(mx/viewport.scale);my=Math.round(my/viewport.scale);
        g.pose().pushMatrix();g.pose().scale(viewport.scale,viewport.scale);
        UiPaint.shadow(g,px,py,pw,ph,12,1);UiPaint.rounded(g,px-1,py-1,pw+2,ph+2,13,palette.border);UiPaint.rounded(g,px,py,pw,ph,12,palette.panel);
        line(g,tr("title_count",store.entries().size()),px+20,py+12,pw-72,26,palette.text);g.fill(px+18,py+48,px+pw-18,py+49,palette.border);
        g.fill(right-13,py+60,right-12,py+ph-18,palette.border);
        drawRows(g,rows,left,listY,listWidth,listBottom,mx,my,partial);
        drawRows(g,options,right,optionsTop,rightWidth,optionsBottom,mx,my,partial);
        if(rows.isEmpty())line(g,tr("empty"),left+4,listY+12,listWidth-8,56,palette.muted);
        scrollbar(g,left+listWidth-2,listY,listBottom,scroll,maxScroll());
        scrollbar(g,right+rightWidth-2,optionsTop,optionsBottom,optionScroll,maxScroll(options,33,optionsTop,optionsBottom));
        line(g,store.settings().global?tr("global_color"):tr("personal_color",selected),right,colorY-24,rightWidth,22,palette.muted);
        UiPaint.rounded(g,left,name.getY(),listWidth-36,30,6,palette.field);
        name.setTextColor(palette.text);name.extractRenderState(g,mx,my,partial);
        hex.frame(palette);hex.extractRenderState(g,mx,my,partial);
        for(var button:controls) { button.frame(palette,1,elapsed);button.extractRenderState(g,mx,my,partial); }
        line(g,status,px+18,py+ph-30,pw-36,22,palette.muted);g.pose().popMatrix();
    }
    private void line(GuiGraphicsExtractor g,Component text,int x,int y,int w,int h,int color) { UiPaint.baseline(g,UiPaint.fit(g,text,w,.88f),x,UiPaint.textBaseline(g,y,h,.88f),.88f,color,1); }
    private MouseButtonEvent adjusted(MouseButtonEvent e) { return new MouseButtonEvent(e.x()/viewport.scale,e.y()/viewport.scale,e.buttonInfo()); }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice) {
        var e=adjusted(event);
        if(e.y()<listY||e.y()>=listBottom)for(var row:rows)row.visible=false;
        if(e.y()<optionsTop||e.y()>=optionsBottom)for(var option:options)option.visible=false;
        boolean handled=super.mouseClicked(e,twice);positionRows();return handled;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(adjusted(event)); }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) { return super.mouseDragged(adjusted(event),dx/viewport.scale,dy/viewport.scale); }
    @Override public void mouseMoved(double x,double y) { super.mouseMoved(x/viewport.scale,y/viewport.scale); }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical) {
        x/=viewport.scale;y/=viewport.scale;
        if(x>=left&&x<left+listWidth&&y>=listY&&y<listBottom){scrollTarget=Math.clamp(scrollTarget-(float)vertical*44,0,maxScroll());return true;}
        if(x>=right&&x<right+rightWidth&&y>=optionsTop&&y<optionsBottom){optionTarget=Math.clamp(optionTarget-(float)vertical*33,0,maxScroll(options,33,optionsTop,optionsBottom));return true;}
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override public boolean keyPressed(KeyEvent event) { if(event.key()==com.mojang.blaze3d.platform.InputConstants.KEY_RETURN&&name.isFocused()&&add.active) { addFriend();return true; }return super.keyPressed(event); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}

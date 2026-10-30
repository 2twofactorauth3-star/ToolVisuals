package ru.he3hauka.toolvisuals.client.connection.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.connection.AccountStore;
import ru.he3hauka.toolvisuals.client.connection.ConnectionIdentity;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public final class AccountScreen extends Screen {
    private static final int ROW_PITCH=58;
    private final Screen parent;
    private final AccountStore store=ToolVisualsClient.ACCOUNTS;
    private final List<VisualsButton> controls=new ArrayList<>();
    private final List<AccountRow> rows=new ArrayList<>();
    private final List<VisualsButton> quickLogin=new ArrayList<>();
    private final Motion scroll=new Motion(0);
    private UiViewport viewport;
    private UiPalette palette=new UiPalette(0);
    private EditBox search,newName;
    private VisualsButton use,favorite,delete,add;
    private GameProfile launcher;
    private Supplier<PlayerSkin> launcherSkin;
    private String selectedName,query="",draft="";
    private Component status=Component.empty();
    private boolean favoritesOnly,wide,small,draggingScroll;
    private int px,py,pw,ph,listX,listY,listWidth,listBottom,detailX,detailWidth;
    private float scrollTarget,scrollGrab,elapsed;
    private long lastFrame;

    private static final class Entry {
        public final String name;
        public final boolean launcher,favorite;
        public Entry(String name,boolean launcher,boolean favorite){this.name=name;this.launcher=launcher;this.favorite=favorite;}
    }
    public AccountScreen(Screen parent){super(tr("title"));this.parent=parent;}
    private static Component tr(String key,Object... args){return UiI18n.tr("toolvisuals.accounts."+key,args);}

    @Override protected void init(){
        if(store.status()==ru.he3hauka.toolvisuals.client.storage.JsonFile.Status.LOAD_FAILED)status=tr("io_error");
        controls.clear();rows.clear();quickLogin.clear();
        viewport=new UiViewport(width,height,minecraft.getWindow().getGuiScale());
        pw=Math.min(720,viewport.width-32);ph=Math.min(484,viewport.height-32);px=(viewport.width-pw)/2;py=(viewport.height-ph)/2;
        wide=pw>=640&&ph>=448;small=ph<380;
        launcher=ConnectionIdentity.launcherProfile(minecraft);
        launcherSkin=minecraft.getSkinManager().createLookup(launcher,true);
        if(selectedName==null)selectedName=minecraft.getUser().getName();
        listX=px+20;listWidth=wide?328:pw-40;
        listY=py+(small?100:140);listBottom=py+ph-(wide?22:112);
        detailX=px+listWidth+44;detailWidth=pw-listWidth-64;
        int searchY=py+(small?58:98);
        search=addWidget(field(listX+28,searchY,listWidth-72,30,tr("search")));
        search.setMaxLength(32);search.setValue(query);
        search.setResponder(value->{query=value;scrollTarget=0;scroll.snap(0);refreshRows();});
        control(listX+listWidth-30,searchY,28,30,tr("favorites"),ICON,UiPaint.Icon.STAR,
                ()->{favoritesOnly=!favoritesOnly;scrollTarget=0;scroll.snap(0);refreshRows();},()->favoritesOnly);
        control(px+pw-40,py+12,26,26,UiI18n.tr("toolvisuals.ui.back"),ICON,UiPaint.Icon.BACK,this::onClose,null);
        if(wide){
            use=control(detailX,py+238,detailWidth,34,tr("use"),CHIP,null,this::activateSelected,()->true);
            favorite=control(detailX,py+280,detailWidth/2-3,30,tr("favorite"),CHIP,UiPaint.Icon.STAR,this::toggleFavorite,()->selected()!=null&&selected().favorite);
            delete=control(detailX+detailWidth/2+3,py+280,detailWidth/2-3,30,tr("delete"),GHOST,UiPaint.Icon.CLOSE,this::deleteSelected,null);
            int y=py+ph-86;
            newName=addWidget(field(detailX+10,y,detailWidth-56,32,tr("new_name")));
            add=control(detailX+detailWidth-34,y,34,32,tr("add"),ICON,UiPaint.Icon.PLUS,this::addProfile,null);
            control(detailX,py+ph-45,detailWidth,28,tr("random"),GHOST,null,this::randomProfile,null);
        }else{
            int y=py+ph-100;
            newName=addWidget(field(px+30,y,pw-146,32,tr("new_name")));
            add=control(px+pw-104,y,34,32,tr("add"),ICON,UiPaint.Icon.PLUS,this::addProfile,null);
            control(px+pw-64,y,34,32,tr("random"),ICON,UiPaint.Icon.RESET,this::randomProfile,null);
            use=control(px+20,py+ph-54,pw-124,34,tr("use"),CHIP,null,this::activateSelected,()->true);
            favorite=control(px+pw-96,py+ph-54,34,34,tr("favorite"),ICON,UiPaint.Icon.STAR,this::toggleFavorite,()->selected()!=null&&selected().favorite);
            delete=control(px+pw-56,py+ph-54,34,34,tr("delete"),ICON,UiPaint.Icon.CLOSE,this::deleteSelected,null);
        }
        newName.setMaxLength(ConnectionIdentity.MAX_NAME_LENGTH);newName.setValue(draft);
        newName.setResponder(value->{draft=value;updateActions();});
        refreshRows();setFocused(search);search.setFocused(true);lastFrame=System.nanoTime();
    }
    private EditBox field(int x,int y,int w,int h,Component label){
        var field=new EditBox(UiFont.get(),x,y,w,h,label){
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
                if(getValue().isEmpty()&&!isFocused()){
                    line(g,label,getX(),getY(),getWidth(),getHeight(),1,palette.muted);return;
                }
                g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(getHeight()));
                super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
            }
        };
        field.setBordered(false);field.setTextShadow(false);return field;
    }
    private VisualsButton control(int x,int y,int w,int h,Component text,VisualsButton.Kind kind,UiPaint.Icon icon,
                                  Runnable action,java.util.function.BooleanSupplier selected){
        var button=addWidget(new VisualsButton(x,y,w,h,text,kind,icon,text,action,selected));controls.add(button);return button;
    }
    private List<Entry> entries(){
        var result=new ArrayList<Entry>();result.add(new Entry(launcher.name(),true,false));
        for(var profile:store.profiles())if(!profile.name.equals(launcher.name()))result.add(new Entry(profile.name,false,profile.favorite));
        return result;
    }
    private Entry selected(){return entries().stream().filter(e->e.name.equals(selectedName)).findFirst().orElse(null);}
    private boolean current(Entry e){return e.launcher?!ConnectionIdentity.custom(minecraft):ConnectionIdentity.custom(minecraft)&&minecraft.getUser().getName().equals(e.name);}
    private void refreshRows(){
        rows.forEach(this::removeWidget);quickLogin.forEach(this::removeWidget);rows.clear();quickLogin.clear();
        String needle=query.strip().toLowerCase(Locale.ROOT);
        for(var entry:entries()){
            if((favoritesOnly&&!entry.launcher&&!entry.favorite)||!entry.name.toLowerCase(Locale.ROOT).contains(needle))continue;
            rows.add(addWidget(new AccountRow(entry)));
            quickLogin.add(addWidget(new VisualsButton(0,0,28,32,tr("use"),ICON,UiPaint.Icon.ARROW,Component.empty(),
                    ()->{selectedName=entry.name;activateSelected();},()->current(entry))));
        }
        if(selected()==null)selectedName=launcher.name();
        scrollTarget=Math.clamp(scrollTarget,0,maxScroll());if(scroll.value()>maxScroll())scroll.snap(scrollTarget);
        positionRows();updateActions();
    }
    private void updateActions(){
        if(use==null||newName==null||add==null)return;
        var entry=selected();use.active=entry!=null&&!current(entry);
        use.setMessage(tr(entry!=null&&current(entry)?"current":"use"));
        favorite.active=delete.active=entry!=null&&!entry.launcher;
        add.active=ConnectionIdentity.valid(newName.getValue())&&store.profiles().size()<AccountStore.LIMIT;
    }
    private void activateSelected(){
        var entry=selected();if(entry==null)return;
        try {
        if(entry.launcher)ConnectionIdentity.restore(minecraft);else ConnectionIdentity.apply(minecraft,entry.name);
        status=tr("switched",minecraft.getUser().getName());refreshRows();
        }catch(java.io.IOException error){status=tr("io_error");}
    }
    private void toggleFavorite(){var e=selected();if(e==null||e.launcher)return;try{store.favorite(e.name,!e.favorite);refreshRows();}catch(java.io.IOException error){status=tr("io_error");}}
    private void deleteSelected(){
        var entry=selected();if(entry==null||entry.launcher)return;
        try {store.remove(entry.name);if(current(entry))ConnectionIdentity.restore(minecraft);
        status=tr("removed",entry.name);selectedName=minecraft.getUser().getName();refreshRows();}catch(java.io.IOException error){status=tr("io_error");}
    }
    private void addProfile(){
        String name=newName.getValue();if(!ConnectionIdentity.valid(name))return;
        if(name.equals(launcher.name())){selectedName=name;status=tr("exists");updateActions();return;}
        try{boolean added=store.add(name);selectedName=name;status=tr(added?"added":"exists",name);}
        catch(IllegalStateException error){status=tr("limit");return;}
        catch(java.io.IOException error){status=tr("io_error");return;}
        newName.setValue("");query="";search.setValue("");favoritesOnly=false;scrollTarget=0;scroll.snap(0);refreshRows();
    }
    private void randomProfile(){
        if(store.profiles().size()>=AccountStore.LIMIT){status=tr("limit");return;}
        newName.setValue(ConnectionIdentity.randomName());addProfile();
    }
    private float maxScroll(){return Math.max(0,rows.size()*ROW_PITCH-4-(listBottom-listY));}
    private void positionRows(){
        for(int i=0;i<rows.size();i++){
            var row=rows.get(i);row.setX(listX);row.setY(listY+i*ROW_PITCH-Math.round(scroll.value()));
            row.visible=row.getY()<listBottom&&row.getY()+54>listY;
            var quick=quickLogin.get(i);quick.setX(listX+listWidth-36);quick.setY(row.getY()+11);quick.visible=row.visible;
        }
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){extractPanorama(g,partial);g.fill(0,0,width,height,0x80070910);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
        if(width!=g.guiWidth()||height!=g.guiHeight()){resize(g.guiWidth(),g.guiHeight());return;}
        long now=System.nanoTime();elapsed=Math.clamp((now-lastFrame)/1e9f,0,1);lastFrame=now;
        scroll.step(scrollTarget,elapsed,22);positionRows();palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);
        mx=Math.round(mx/viewport.scale);my=Math.round(my/viewport.scale);
        g.pose().pushMatrix();g.pose().scale(viewport.scale,viewport.scale);
        UiPaint.shadow(g,px,py,pw,ph,12,1);UiPaint.rounded(g,px-1,py-1,pw+2,ph+2,13,palette.border);
        UiPaint.rounded(g,px,py,pw,ph,12,palette.panel);
        UiPaint.icon(g,UiPaint.Icon.PLAYER,px+20,py+18,15,palette.accentText,1);
        line(g,getTitle(),px+44,py+12,pw-100,26,1.05f,palette.text);
        g.fill(px+20,py+49,px+pw-20,py+50,palette.border);
        if(!small)line(g,tr("active",minecraft.getUser().getName()),px+20,py+60,pw-40,24,.9f,palette.accentText);
        UiPaint.rounded(g,listX,search.getY(),listWidth-36,30,6,palette.field);
        UiPaint.icon(g,UiPaint.Icon.SEARCH,listX+8,search.getY()+8,14,palette.muted,1);
        search.setTextColor(palette.text);search.extractRenderState(g,mx,my,partial);
        g.enableScissor(listX,listY,listX+listWidth,listBottom);
        for(int i=0;i<rows.size();i++){
            rows.get(i).extractRenderState(g,mx,my>=listY&&my<listBottom?my:-1000,partial);
            var b=quickLogin.get(i);b.frame(palette,1,elapsed);b.extractRenderState(g,mx,my>=listY&&my<listBottom?my:-1000,partial);
        }
        g.disableScissor();
        if(rows.isEmpty())line(g,tr("empty"),listX+10,listY+18,listWidth-20,30,.9f,palette.muted);
        if(maxScroll()>0){float track=listBottom-listY,thumb=Math.max(20,track*track/(track+maxScroll()));
            UiPaint.rounded(g,listX+listWidth-2,listY,2,track,1,palette.track);
            UiPaint.rounded(g,listX+listWidth-2,listY+(track-thumb)*scroll.value()/maxScroll(),2,thumb,1,palette.accent);}
        if(wide){
            g.fill(detailX-12,py+98,detailX-11,py+ph-22,palette.border);
            var selected=selected();
            if(selected!=null){
                face(g,selected,detailX+(detailWidth-64)/2f,py+112,64);
                String name=UiPaint.fit(g,Component.literal(selected.name),detailWidth,1.15f);
                UiPaint.centered(g,name,detailX,py+185,detailWidth,26,1.15f,palette.text,1);
                UiPaint.centered(g,tr(selected.launcher?"launcher":"offline").getString(),detailX,py+212,detailWidth,20,.8f,palette.muted,1);
            }
            line(g,status,detailX,py+314,detailWidth,18,.8f,palette.accentText);
            line(g,tr("add"),detailX,py+ph-112,detailWidth,20,.85f,palette.muted);
            UiPaint.rounded(g,detailX,newName.getY(),detailWidth-40,32,6,palette.field);
        }else{
            UiPaint.rounded(g,px+20,newName.getY(),pw-126,32,6,palette.field);
        }
        newName.setTextColor(draft.isEmpty()||ConnectionIdentity.valid(draft)?palette.text:0xffff879c);
        newName.extractRenderState(g,mx,my,partial);
        for(var control:controls){control.frame(palette,1,elapsed);control.extractRenderState(g,mx,my,partial);}
        for(var control:controls)if(control.showsHint()){
            String text=UiPaint.fit(g,control.detail,Math.min(320,viewport.width-40),.8f);float w=UiPaint.textWidth(g,text,.8f)+20;
            float x=Math.clamp(mx+12,8,viewport.width-w-8),y=Math.clamp(my+18,8,viewport.height-36);
            g.nextStratum();UiPaint.rounded(g,x,y,w,27,6,palette.hover);UiPaint.centered(g,text,x,y,w,27,.8f,palette.text,1);break;
        }
        g.pose().popMatrix();
    }
    private void line(GuiGraphicsExtractor g,Component text,float x,float y,float w,float h,float scale,int color){
        UiPaint.baseline(g,UiPaint.fit(g,text,w,scale),x,UiPaint.textBaseline(g,y,h,scale),scale,color,1);
    }
    private void face(GuiGraphicsExtractor g,Entry entry,float x,float y,float size){
        var skin=entry.launcher?launcherSkin.get():DefaultPlayerSkin.get(ConnectionIdentity.offlineId(entry.name));
        var texture=skin.body().texturePath();
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(size/8,size/8);
        g.blit(RenderPipelines.GUI_TEXTURED,texture,0,0,8,8,8,8,64,64,0xffffffff);
        g.blit(RenderPipelines.GUI_TEXTURED,texture,0,0,40,8,8,8,64,64,0xffffffff);
        g.pose().popMatrix();
    }
    private final class AccountRow extends Button {
        public final Entry entry;
        AccountRow(Entry entry){super(0,0,listWidth-40,54,Component.literal(entry.name),b->{selectedName=entry.name;updateActions();},DEFAULT_NARRATION);this.entry=entry;}
        @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial){
            boolean chosen=entry.name.equals(selectedName);
            UiPaint.rounded(g,getX(),getY(),listWidth-5,54,8,chosen?palette.hover:palette.raised);
            face(g,entry,getX()+10,getY()+11,32);
            line(g,Component.literal(entry.name),getX()+54,getY()+5,getWidth()-62,25,1.05f,palette.text);
            Component detail=tr(entry.launcher?"launcher":"offline");
            if(current(entry))detail=tr("current_detail",detail);
            line(g,detail,getX()+54,getY()+30,getWidth()-62,18,.75f,current(entry)?palette.accentText:palette.muted);
            if(entry.favorite)UiPaint.icon(g,UiPaint.Icon.STAR_FILLED,getX()+34,getY()+32,12,palette.accentText,1);
        }
    }
    private MouseButtonEvent adjusted(MouseButtonEvent e){return new MouseButtonEvent(e.x()/viewport.scale,e.y()/viewport.scale,e.buttonInfo());}
    @Override public boolean mouseClicked(MouseButtonEvent raw,boolean twice){
        var e=adjusted(raw);
        if(e.button()==InputConstants.MOUSE_BUTTON_LEFT&&maxScroll()>0&&e.x()>=listX+listWidth-7&&e.x()<=listX+listWidth+3&&e.y()>=listY&&e.y()<listBottom){
            float track=listBottom-listY,thumb=Math.max(20,track*track/(track+maxScroll()));float top=listY+(track-thumb)*scroll.value()/maxScroll();
            scrollGrab=e.y()>=top&&e.y()<top+thumb?(float)e.y()-top:thumb/2;draggingScroll=true;dragScroll(e.y());return true;
        }
        if(e.y()<listY||e.y()>=listBottom){rows.forEach(b->b.visible=false);quickLogin.forEach(b->b.visible=false);}
        if(twice&&e.button()==InputConstants.MOUSE_BUTTON_LEFT)for(var row:List.copyOf(rows))
            if(row.visible&&row.isMouseOver(e.x(),e.y())){selectedName=row.entry.name;activateSelected();return true;}
        boolean result=super.mouseClicked(e,twice);positionRows();
        if(getFocused()!=null&&!children().contains(getFocused()))setFocused(null);return result;
    }
    private void dragScroll(double y){
        float track=listBottom-listY,thumb=Math.max(20,track*track/(track+maxScroll()));
        scrollTarget=Math.clamp((float)(y-listY-scrollGrab)/Math.max(1,track-thumb)*maxScroll(),0,maxScroll());scroll.snap(scrollTarget);positionRows();
    }
    @Override public void mouseMoved(double x,double y){super.mouseMoved(x/viewport.scale,y/viewport.scale);}
    @Override public boolean mouseReleased(MouseButtonEvent e){draggingScroll=false;return super.mouseReleased(adjusted(e));}
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(draggingScroll){dragScroll(adjusted(e).y());return true;}return super.mouseDragged(adjusted(e),dx/viewport.scale,dy/viewport.scale);}
    @Override public boolean mouseScrolled(double x,double y,double h,double v){double yy=y/viewport.scale;if(yy>=listY&&yy<listBottom){scrollTarget=Math.clamp(scrollTarget-(float)v*42,0,maxScroll());return true;}return false;}
    @Override public boolean keyPressed(KeyEvent e){
        if(e.hasControlDown()&&e.key()==InputConstants.KEY_F){setFocused(search);search.setFocused(true);return true;}
        if(e.key()==InputConstants.KEY_RETURN&&newName.isFocused()){addProfile();return true;}
        if(e.key()==InputConstants.KEY_RETURN&&getFocused() instanceof AccountRow row){selectedName=row.entry.name;activateSelected();return true;}
        if(e.key()==InputConstants.KEY_DELETE&&!newName.isFocused()&&!search.isFocused()){deleteSelected();return true;}
        if(e.key()==InputConstants.KEY_PAGEUP||e.key()==InputConstants.KEY_PAGEDOWN){scrollTarget=Math.clamp(scrollTarget+(e.key()==InputConstants.KEY_PAGEUP?-1:1)*(listBottom-listY),0,maxScroll());return true;}
        return super.keyPressed(e);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}

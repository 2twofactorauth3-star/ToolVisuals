package ru.he3hauka.toolvisuals.client.waypoint.ui;

import java.util.*;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.color.HexColorField;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer;
import ru.he3hauka.toolvisuals.client.waypoint.WaypointStore;
import ru.he3hauka.toolvisuals.client.waypoint.Waypoints;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public final class WaypointsScreen extends Screen {
    private final Screen parent;
    private final List<VisualsButton> controls=new ArrayList<>();
    private final List<Row> rows=new ArrayList<>();
    private final List<EditBox> fields=new ArrayList<>();
    private UiViewport viewport;
    private UiPalette palette=new UiPalette(0);
    private EditBox name,x,y,z;
    private HexColorField color;
    private VisualsButton save;
    private UUID selected;
    private ColorStyle draftStyle;
    private String draftName="",draftX="",draftY="",draftZ="",draftColor="A896FF",status="";
    private int px,py,pw,ph,listY,listBottom;
    private float scroll;
    private static final class Row {
        public final WaypointStore.Point point;
        public final VisualsButton label,toggle,remove;
        Row(WaypointStore.Point point,VisualsButton label,VisualsButton toggle,VisualsButton remove){this.point=point;this.label=label;this.toggle=toggle;this.remove=remove;}
    }
    public WaypointsScreen(Screen parent){super(tr("title"));this.parent=parent;}
    private static Component tr(String key,Object... args){return UiI18n.tr("toolvisuals.waypoints."+key,args);}
    @Override protected void init() {
        fields.clear();controls.clear();rows.clear();viewport=new UiViewport(width,height,minecraft.getWindow().getGuiScale());
        pw=Math.min(760,viewport.width-32);ph=Math.min(530,viewport.height-32);px=(viewport.width-pw)/2;py=(viewport.height-ph)/2;
        control(px+pw-40,py+12,26,26,UiI18n.tr("toolvisuals.ui.back"),ICON,UiPaint.Icon.BACK,this::onClose,null);
        if(draftName.isEmpty()&&minecraft.player!=null)fillCurrent();
        name=field(px+28,py+70,pw-192,32,"name",draftName,v->draftName=v,40);
        save=control(px+pw-150,py+70,130,32,tr("save"),CHIP,UiPaint.Icon.CHECK,this::save,null);
        int unit=(pw-32)/4;
        x=field(px+28,py+118,unit-24,30,"x",draftX,v->draftX=v,16);
        y=field(px+28+unit,py+118,unit-24,30,"y",draftY,v->draftY=v,16);
        z=field(px+28+unit*2,py+118,unit-24,30,"z",draftZ,v->draftZ=v,16);
        color=addWidget(new HexColorField(px+20+unit*3,py+118,pw-40-unit*3,30,tr("color"),draftColor,this::openColor));
        color.setResponder(value->{draftColor=value;draftStyle=null;updateActions();});fields.add(color);
        control(px+20,py+154,(pw-48)/2,30,tr("here"),CHIP,UiPaint.Icon.PLUS,()->{fillCurrent();rebuildWidgets();},null);
        control(px+28+(pw-48)/2,py+154,(pw-48)/2,30,tr("new"),GHOST,null,()->{selected=null;draftName="";draftStyle=null;fillCurrent();rebuildWidgets();},null);
        listY=py+200;listBottom=py+ph-48;refresh();updateActions();
    }
    private void openColor(){
        var parsed=color.color();int rgb=parsed==null?0xb69aff:parsed;
        minecraft.gui.setScreen(new ColorEditorScreen(this,tr("color"),draftStyle==null?ColorStyle.solid(rgb):draftStyle,style->{
            draftStyle=style;draftColor=ru.he3hauka.toolvisuals.client.config.color.HexColor.format(style.colors.getFirst(),true);
        }));
    }
    private VisualsButton control(int x,int y,int w,int h,Component text,VisualsButton.Kind kind,UiPaint.Icon icon,Runnable action,java.util.function.BooleanSupplier on) {
        var button=addWidget(new VisualsButton(x,y,w,h,text,kind,icon,text,action,on));controls.add(button);return button;
    }
    private EditBox field(int left,int top,int width,int height,String key,String value,java.util.function.Consumer<String> responder,int limit) {
        var field=addWidget(new EditBox(UiFont.get(),left,top,width,height,tr(key)) {
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
                if(getValue().isEmpty()){UiPaint.centered(g,tr(key).getString(),getX(),getY(),getWidth(),getHeight(),.85f,palette.muted,1);if(!isFocused())return;}
                g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(getHeight()));super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
            }
        });field.setBordered(false);field.setTextShadow(false);field.setMaxLength(limit);field.setValue(value);
        field.setResponder(v->{responder.accept(v);updateActions();});fields.add(field);return field;
    }
    private void fillCurrent() {
        if(minecraft.player==null)return;var player=minecraft.player;
        draftX=number(player.getX());draftY=number(player.getY());draftZ=number(player.getZ());
        if(draftName.isBlank())draftName=tr("default_name",ToolVisualsClient.WAYPOINTS.entries(Waypoints.context()).size()+1).getString();
    }
    private static String number(double number){return String.format(Locale.ROOT,"%.1f",number);}
    private WaypointStore.Point draft() {
        var old=ToolVisualsClient.WAYPOINTS.entries(Waypoints.context()).stream().filter(p->p.id.equals(selected)).findFirst().orElse(null);
        return new WaypointStore.Point(selected==null?UUID.randomUUID():selected,Waypoints.context(),old==null?Waypoints.dimension():old.dimension,draftName,
                Double.parseDouble(draftX.replace(',','.')),Double.parseDouble(draftY.replace(',','.')),Double.parseDouble(draftZ.replace(',','.')),
                draftStyle==null?ColorStyle.solid(ru.he3hauka.toolvisuals.client.config.color.HexColor.require(draftColor)):draftStyle,old==null||old.enabled,old!=null&&old.death);
    }
    private void updateActions(){if(save==null)return;try{draft();save.active=ru.he3hauka.toolvisuals.client.config.color.HexColor.parse(draftColor)!=null;}catch(RuntimeException error){save.active=false;}}
    private void save(){try{var point=draft();ToolVisualsClient.WAYPOINTS.put(point);selected=point.id;status=tr("saved").getString();SoundCustomizer.notice();refresh();}catch(java.io.IOException|RuntimeException error){status=tr("save_error").getString();}}
    private void select(WaypointStore.Point point){selected=point.id;draftName=point.death?tr("death").getString():point.name;draftX=number(point.x);draftY=number(point.y);draftZ=number(point.z);draftStyle=point.style;draftColor=ru.he3hauka.toolvisuals.client.config.color.HexColor.format(point.color,false);rebuildWidgets();}
    private void refresh() {
        for(var row:rows){removeWidget(row.label);removeWidget(row.toggle);removeWidget(row.remove);}rows.clear();
        for(var p:ToolVisualsClient.WAYPOINTS.entries(Waypoints.context())) {
            String dimension=switch(p.dimension){case "minecraft:overworld"->tr("overworld").getString();case "minecraft:the_nether"->tr("nether").getString();case "minecraft:the_end"->tr("end").getString();default->p.dimension.substring(p.dimension.indexOf(':')+1);};
            String label=(p.death?tr("death").getString():p.name)+" В· "+number(p.x)+", "+number(p.y)+", "+number(p.z)+" В· "+dimension;
            var name=addWidget(new VisualsButton(0,0,pw-132,36,Component.literal(label),MENU,null,tr("edit"),()->select(p),()->p.id.equals(selected)));
            var toggle=addWidget(new VisualsButton(0,0,34,30,tr("visible"),SWITCH,null,tr("visible"),()->change(()->ToolVisualsClient.WAYPOINTS.put(new WaypointStore.Point(p.id,p.context,p.dimension,p.name,p.x,p.y,p.z,p.style,!p.enabled,p.death))),()->p.enabled));
            var remove=addWidget(new VisualsButton(0,0,28,30,tr("remove"),ICON,UiPaint.Icon.CLOSE,tr("remove"),()->change(()->{ToolVisualsClient.WAYPOINTS.remove(p.id);if(p.id.equals(selected))selected=null;}),null));
            rows.add(new Row(p,name,toggle,remove));
        }
        scroll=Math.clamp(scroll,0,maxScroll());position();if(getFocused()!=null&&!children().contains(getFocused()))setFocused(null);
    }
    @FunctionalInterface private interface Operation { void run() throws java.io.IOException; }
    private void change(Operation operation){try{operation.run();refresh();}catch(java.io.IOException|RuntimeException error){status=tr("save_error").getString();}}
    private float maxScroll(){return Math.max(0,rows.size()*42-(listBottom-listY));}
    private void position(){for(int i=0;i<rows.size();i++){var row=rows.get(i);int y=listY+i*42-Math.round(scroll);row.label.setX(px+20);row.label.setY(y);row.toggle.setX(px+pw-100);row.toggle.setY(y+3);row.remove.setX(px+pw-54);row.remove.setY(y+3);row.label.visible=row.toggle.visible=row.remove.visible=y<listBottom&&y+36>listY;}}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,0x90070910);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        if(width!=g.guiWidth()||height!=g.guiHeight()){resize(g.guiWidth(),g.guiHeight());return;}
        palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);mx=Math.round(mx/viewport.scale);my=Math.round(my/viewport.scale);
        g.pose().pushMatrix();g.pose().scale(viewport.scale,viewport.scale);
        UiPaint.shadow(g,px,py,pw,ph,12,1);UiPaint.rounded(g,px-1,py-1,pw+2,ph+2,13,palette.border);UiPaint.rounded(g,px,py,pw,ph,12,palette.panel);
        UiPaint.centered(g,getTitle().getString(),px+20,py+12,pw-72,28,1,palette.text,1);g.fill(px+20,py+49,px+pw-20,py+50,palette.border);
        for(var field:fields){
            if(field instanceof HexColorField hex)hex.frame(palette);
            else{UiPaint.rounded(g,field.getX()-8,field.getY(),field.getWidth()+16,field.getHeight(),6,palette.field);field.setTextColor(palette.text);}
            field.extractRenderState(g,mx,my,partial);
        }
        for(int i=1;i<fields.size();i++)UiPaint.text(g,tr(new String[]{"x","y","z","color"}[i-1]),fields.get(i).getX(),fields.get(i).getY()-11,.65f,palette.muted,1);
        for(var button:controls){button.frame(palette,1,1/60f);button.extractRenderState(g,mx,my,partial);}
        g.enableScissor(px+18,listY,px+pw-18,listBottom);
        for(var row:rows)for(var button:List.of(row.label,row.toggle,row.remove)){button.frame(palette,1,1/60f);button.extractRenderState(g,mx,my>=listY&&my<listBottom?my:-1000,partial);}
        g.disableScissor();if(rows.isEmpty())UiPaint.centered(g,tr(Waypoints.context().isEmpty()?"no_world":"empty").getString(),px+24,listY,pw-48,36,.85f,palette.muted,1);
        UiPaint.centered(g,UiPaint.fit(g,status.isEmpty()?tr("help"):Component.literal(status),pw-40,.8f),px+20,py+ph-38,pw-40,26,.8f,palette.muted,1);g.pose().popMatrix();
    }
    private MouseButtonEvent adjusted(MouseButtonEvent e){return new MouseButtonEvent(e.x()/viewport.scale,e.y()/viewport.scale,e.buttonInfo());}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){var e=adjusted(event);if(e.y()<listY||e.y()>=listBottom)for(var row:rows){row.label.visible=row.toggle.visible=row.remove.visible=false;}boolean result=super.mouseClicked(e,twice);if(getFocused()!=null&&!children().contains(getFocused()))setFocused(null);position();return result;}
    @Override public boolean mouseReleased(MouseButtonEvent event){return super.mouseReleased(adjusted(event));}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){return super.mouseDragged(adjusted(event),dx/viewport.scale,dy/viewport.scale);}
    @Override public void mouseMoved(double x,double y){super.mouseMoved(x/viewport.scale,y/viewport.scale);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){scroll=Math.clamp(scroll-(float)vertical*42,0,maxScroll());position();return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}

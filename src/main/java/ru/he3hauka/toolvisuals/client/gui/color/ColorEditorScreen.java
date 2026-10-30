package ru.he3hauka.toolvisuals.client.gui.color;

import java.awt.Color;
import java.util.*;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.render.UiSprites;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;

public final class ColorEditorScreen extends VisualEditorScreen {
    private final Consumer<ColorStyle> apply;
    private final boolean spatial,single;
    private final ArrayList<Integer> colors;
    private ColorStyle.Mode mode;
    private ColorStyle.Space space;
    private double duration,hold,phase,spread;
    private boolean smooth,reverse,updating;
    private int selected,page,drag=-1;
    private float hue,saturation,brightness;
    private HexColorField hex;
    private static final int[] CHANNEL_COLORS={0xff8f9e,0x7cebc5,0x82caff};
    private VisualsButton save;
    private AbstractWidget timing,holding;
    private final EditBox[] rgb=new EditBox[3];
    public ColorEditorScreen(Screen parent,Component name,ColorStyle initial,Consumer<ColorStyle> apply) {
        this(parent,name,initial,apply,false);
    }
    public ColorEditorScreen(Screen parent,Component name,ColorStyle initial,Consumer<ColorStyle> apply,boolean spatial) {
        this(parent,name,initial,apply,spatial,false);
    }
    public static ColorEditorScreen single(Screen parent,Component name,int initial,java.util.function.IntConsumer apply){
        return new ColorEditorScreen(parent,name,ColorStyle.solid(initial),style->apply.accept(style.colors.getFirst()),false,true);
    }
    private ColorEditorScreen(Screen parent,Component name,ColorStyle initial,Consumer<ColorStyle> apply,boolean spatial,boolean single) {
        super(parent,tr("color_title",name));this.apply=apply;this.spatial=spatial;this.single=single;var c=ColorStyle.safe(initial,0xffffff);colors=new ArrayList<>(c.colors);mode=c.mode;space=c.space;duration=c.duration;hold=c.hold;phase=c.phase;spread=c.spread;smooth=c.smooth;reverse=c.reverse;readColor();
    }
    private ColorStyle draft(){return new ColorStyle(colors,mode,space,duration,hold,phase,spread,smooth,reverse);}
    private void readColor(){float[] hsv=Color.RGBtoHSB(colors.get(selected)>>16&255,colors.get(selected)>>8&255,colors.get(selected)&255,null);hue=hsv[0];saturation=hsv[1];brightness=hsv[2];}
    @Override protected void init() {
        frame(single?278:700,474);
        if(single){
            colorFields();
            button(20,434,112,28,tr("cancel"),null,this::onClose,null);
            save=button(140,434,118,28,tr("apply"),UiPaint.Icon.CHECK,()->{apply.accept(draft());onClose();},null);
            return;
        }
        for(int i=0;i<8;i++) {
            final int index=page*8+i;if(index>=colors.size())break;
            button(270+i*40,82,32,30,Component.empty(),null,()->{selected=index;readColor();rebuildWidgets();},()->selected==index);
        }
        button(596,82,32,30,Component.literal("\u2039"),null,()->{page=Math.max(0,page-1);rebuildWidgets();},null).active=page>0;
        button(636,82,44,30,Component.literal("\u203a"),null,()->{page=Math.min((colors.size()-1)/8,page+1);rebuildWidgets();},null).active=(page+1)*8<colors.size();
        button(270,120,140,28,tr("add_color"),UiPaint.Icon.PLUS,()->{if(colors.size()<ColorStyle.LIMIT){colors.add(colors.get(selected));selected=colors.size()-1;page=selected/8;if(mode==ColorStyle.Mode.STATIC)mode=ColorStyle.Mode.CYCLE;rebuildWidgets();}},null).active=colors.size()<ColorStyle.LIMIT;
        button(418,120,122,28,tr("remove_color"),null,()->{if(colors.size()>1){colors.remove(selected);selected=Math.min(selected,colors.size()-1);page=selected/8;readColor();rebuildWidgets();}},null).active=colors.size()>1;
        button(548,120,62,28,Component.literal("\u2190"),null,()->move(-1),null).active=selected>0;
        button(618,120,62,28,Component.literal("\u2192"),null,()->move(1),null).active=selected<colors.size()-1;
        for(var next:ColorStyle.Mode.values())button(270+next.ordinal()*138,156,132,28,tr("mode_"+next.name().toLowerCase(Locale.ROOT)),null,()->{mode=next;},()->mode==next);
        button(270,192,68,28,Component.literal("RGB"),null,()->space=ColorStyle.Space.RGB,()->space==ColorStyle.Space.RGB);
        button(344,192,68,28,Component.literal("HSV"),null,()->space=ColorStyle.Space.HSV,()->space==ColorStyle.Space.HSV);
        button(418,192,126,28,tr("smooth"),null,()->smooth=!smooth,()->smooth);
        button(550,192,130,28,tr("reverse"),null,()->reverse=!reverse,()->reverse);
        timing=slider(270,230,410,tr("duration"),.2,120,.2,()->duration,v->duration=v,v->String.format(Locale.ROOT,"%.1f s",v));
        holding=slider(270,270,410,tr("hold"),0,.9,.01,()->hold,v->hold=v,v->Math.round(v*100)+"%");
        slider(270,310,410,tr("phase"),0,1,.01,()->phase,v->phase=v,v->Math.round(v*100)+"%");
        if(spatial)slider(270,350,410,tr("spread"),0,2,.05,()->spread,v->spread=v,v->Math.round(v*100)+"%");
        colorFields();
        button(20,434,224,28,tr("cancel"),null,this::onClose,null);
        save=button(270,434,410,28,tr("apply"),UiPaint.Icon.CHECK,()->{apply.accept(draft());onClose();},null);
    }
    private void colorFields(){
        for(int i=0;i<3;i++){final int channel=i;rgb[i]=field(26+i*76,296,56,"",3,value->typedRgb(channel,value));}
        hex=hexField(20,354,224,"",this::typedHex);syncFields();
    }
    private void move(int direction){int next=selected+direction;if(next<0||next>=colors.size())return;Collections.swap(colors,selected,next);selected=next;page=selected/8;rebuildWidgets();}
    private void typedRgb(int channel,String value) {
        if(updating)return;
        try {int n=Integer.parseInt(value);if(n<0||n>255)return;int shift=(2-channel)*8;colors.set(selected,colors.get(selected)&~(255<<shift)|n<<shift);readColor();syncFieldsExcept(rgb[channel]);}catch(NumberFormatException ignored){}
    }
    private void typedHex(String value) {
        if(updating)return;var parsed=ru.he3hauka.toolvisuals.client.config.color.HexColor.parse(value);if(parsed==null)return;
        colors.set(selected,parsed);readColor();syncFieldsExcept(hex);
    }
    private void syncFields(){syncFieldsExcept(null);}
    private void syncFieldsExcept(EditBox except) {
        updating=true;int color=colors.get(selected);
        for(int i=0;i<3;i++)if(rgb[i]!=null&&rgb[i]!=except)rgb[i].setValue(Integer.toString(color>>(2-i)*8&255));
        if(hex!=null&&hex!=except)hex.setValue(ru.he3hauka.toolvisuals.client.config.color.HexColor.format(color,true));updating=false;
    }
    private void pick(double x,double y) {
        if(drag==0){double dx=x-116,dy=y-174;hue=(float)((Math.atan2(dy,dx)/Math.TAU+1)%1);saturation=(float)Math.min(1,Math.hypot(dx,dy)/96);}
        if(drag==1)brightness=(float)Math.clamp(1-(y-78)/192,0,1);
        colors.set(selected,Color.HSBtoRGB(hue,saturation,brightness)&0xffffff);syncFields();
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my) {
        save.active=valid(hex)&&Arrays.stream(rgb).allMatch(this::valid);
        if(!single)timing.active=holding.active=mode!=ColorStyle.Mode.STATIC&&colors.size()>1;
        UiPaint.text(g,tr("wheel"),px+20,py+58,.8f,palette.muted,1);UiSprites.colorWheel(g,px+20,py+78,192);
        double angle=hue*Math.TAU;float cx=px+116+(float)Math.cos(angle)*saturation*96,cy=py+174+(float)Math.sin(angle)*saturation*96;
        UiPaint.rounded(g,cx-5,cy-5,10,10,5,0xff0b0b12);UiPaint.rounded(g,cx-3.5f,cy-3.5f,7,7,3.5f,0xffffffff);UiPaint.rounded(g,cx-2,cy-2,4,4,2,0xff000000|colors.get(selected));
        for(int i=0;i<192;i++)g.fill(px+224,py+78+i,px+244,py+79+i,0xff000000|Color.HSBtoRGB(hue,saturation,1-i/191f));
        int handle=py+78+Math.round((1-brightness)*191);g.outline(px+221,handle-2,26,5,0xffffffff);
        for(int i=0;i<3;i++)UiPaint.text(g,new String[]{"R","G","B"}[i],px+20+i*76,py+278,.8f,ColorContrast.readable(CHANNEL_COLORS[i],palette.panel,palette.text),1);
        UiPaint.text(g,"HEX",px+20,py+336,.8f,palette.accentText,1);
        if(!single)UiPaint.text(g,tr("palette",colors.size(),ColorStyle.LIMIT),px+270,py+58,.8f,palette.muted,1);
        UiPaint.rounded(g,px+20,py+402,224,28,6,0xff000000|draft().sample(ColorStyle.clock(),0));
        if(single)return;
        var style=draft();for(int i=0;i<205;i++){int color=spatial?style.sample(ColorStyle.clock(),i/204d):style.sample(ColorStyle.clock()+i/204d*style.duration,0);g.fill(px+270+i*2,py+402,px+272+i*2,py+430,0xff000000|color);}
        if(!spatial)UiPaint.text(g,tr("cycle_preview"),px+270,py+368,.75f,palette.muted,1);
    }
    private boolean valid(EditBox field){if(field==hex)return hex.color()!=null;try{int n=Integer.parseInt(field.getValue());return n>=0&&n<=255;}catch(NumberFormatException error){return false;}}
    @Override protected int fieldColor(EditBox field){
        if(!valid(field))return ColorContrast.readable(0xff778b,palette.field,palette.text);
        for(int channel=0;channel<rgb.length;channel++)if(field==rgb[channel])return ColorContrast.readable(CHANNEL_COLORS[channel],palette.field,palette.text);
        return palette.accentText;
    }
    @Override protected void overlay(GuiGraphicsExtractor g){if(single)return;for(int i=0;i<8;i++){int index=page*8+i;if(index>=colors.size())break;int x=px+274+i*40;UiPaint.rounded(g,x-2,py+86,28,22,5,index==selected?palette.accentText:palette.border);UiPaint.rounded(g,x,py+88,24,18,4,0xff000000|colors.get(index));}}
    @Override public boolean mouseClicked(MouseButtonEvent raw,boolean twice) {
        var e=adjusted(raw);double x=e.x()-px,y=e.y()-py;
        if(e.button()==InputConstants.MOUSE_BUTTON_LEFT) {
            if(Math.hypot(x-116,y-174)<=96)drag=0;else if(x>=221&&x<247&&y>=78&&y<270)drag=1;
            if(drag>=0){setFocused(null);pick(x,y);return true;}
        }
        return super.mouseClicked(raw,twice);
    }
    @Override public boolean mouseDragged(MouseButtonEvent raw,double dx,double dy){if(drag>=0){var e=adjusted(raw);pick(e.x()-px,e.y()-py);return true;}return super.mouseDragged(raw,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent e){drag=-1;return super.mouseReleased(e);}
}

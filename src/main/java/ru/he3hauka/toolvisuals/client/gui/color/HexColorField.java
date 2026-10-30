package ru.he3hauka.toolvisuals.client.gui.color;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.config.color.HexColor;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class HexColorField extends EditBox {
    private static final int LEFT=8,RIGHT=32;
    private final Runnable picker;
    private final Motion focus=new Motion(0);
    private UiPalette palette=new UiPalette(0);
    private long lastFrame;
    private String parsedValue;
    private Integer parsedColor;
    private boolean overSwatch;
    private int previousRgb,previousBackground,previousFallback,foreground;
    private boolean cachedForeground;

    public HexColorField(int x,int y,int width,int height,Component label,String value,Runnable picker){
        super(UiFont.get(),x+LEFT,y,Math.max(1,width-LEFT-RIGHT),height,label);
        this.picker=picker;
        setBordered(false);setTextShadow(false);setMaxLength(7);
        var initial=HexColor.parse(value);setValue(initial==null?value:HexColor.format(initial,true));
        setHint(Component.literal("#RRGGBB"));
        setTooltipDelay(java.time.Duration.ofMillis(450));
        setTooltip(Tooltip.create(UiI18n.tr(picker==null?"toolvisuals.color.hex_hint":"toolvisuals.color.hex_picker_hint")));
    }

    public void frame(UiPalette palette){this.palette=palette;}

    public Integer color(){
        String value=getValue();
        if(!value.equals(parsedValue)){parsedValue=value;parsedColor=HexColor.parse(value);}
        return parsedColor;
    }

    private int textColor(int rgb,int background){
        if(!cachedForeground||previousRgb!=rgb||previousBackground!=background||previousFallback!=palette.text){
            foreground=ColorContrast.readable(rgb,background,palette.text);
            previousRgb=rgb;previousBackground=background;previousFallback=palette.text;cachedForeground=true;
        }
        return foreground;
    }

    @Override public boolean isMouseOver(double x,double y){
        return visible&&x>=getX()-LEFT&&x<getX()+getWidth()+RIGHT&&y>=getY()&&y<getY()+getHeight();
    }

    private boolean swatch(double x,double y){return isMouseOver(x,y)&&x>=getX()+getWidth()+3;}

    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){
        if(active&&visible&&picker!=null&&event.button()==InputConstants.MOUSE_BUTTON_LEFT&&swatch(event.x(),event.y())){
            picker.run();return true;
        }
        return super.mouseClicked(event,twice);
    }

    @Override public boolean keyPressed(KeyEvent event){
        if(active&&isFocused()&&picker!=null&&event.key()==InputConstants.KEY_RETURN&&event.hasControlDown()){
            picker.run();return true;
        }
        return super.keyPressed(event);
    }

    @Override protected void handleCursor(GuiGraphicsExtractor graphics){
        if(active&&overSwatch&&picker!=null)graphics.requestCursor(CursorTypes.POINTING_HAND);
        else super.handleCursor(graphics);
    }

    @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial){
        long now=System.nanoTime();float elapsed=lastFrame==0?1/60f:Math.clamp((now-lastFrame)/1e9f,0,.1f);lastFrame=now;
        isHovered=isMouseOver(mouseX,mouseY);overSwatch=swatch(mouseX,mouseY);
        float amount=focus.step(isFocused()?1:isHovered?.55f:0,elapsed,18),opacity=active?1:.45f;
        Integer parsed=color();boolean valid=parsed!=null;int rgb=valid?parsed:0xff778b;
        int fill=UiPalette.mix(palette.field,0xff000000|rgb,valid?.065f+amount*.035f:.07f);
        int border=UiPalette.mix(palette.border,valid?palette.accent:0xffff778b,.3f+amount*.55f);
        float x=getX()-LEFT,y=getY(),w=getWidth()+LEFT+RIGHT,h=getHeight();
        if(amount>.01f)UiPaint.rounded(g,x-2,y-2,w+4,h+4,8,UiPaint.alpha(valid?palette.accent:0xffff778b,amount*.1f*opacity));
        UiPaint.rounded(g,x,y,w,h,7,UiPaint.alpha(border,opacity));
        UiPaint.rounded(g,x+1,y+1,w-2,h-2,6,UiPaint.alpha(fill,opacity));
        float size=Math.min(18,h-10),sx=x+w-8-size,sy=y+(h-size)/2;
        UiPaint.rounded(g,sx-1,sy-1,size+2,size+2,5,UiPaint.alpha(ColorContrast.readable(rgb,fill,palette.accentText),opacity));
        UiPaint.rounded(g,sx,sy,size,size,4,UiPaint.alpha(valid?0xff000000|rgb:palette.field,opacity));
        if(!valid)UiPaint.icon(g,UiPaint.Icon.CLOSE,sx+3,sy+3,size-6,0xffff778b,opacity);
        setTextColor(UiPaint.alpha(textColor(rgb,fill),opacity));
        setTextColorUneditable(UiPaint.alpha(palette.muted,opacity));
        g.pose().pushMatrix();
        try{g.pose().translate(0,UiFont.centeredTextOffset(getHeight()));super.extractWidgetRenderState(g,mouseX,mouseY,partial);}
        finally{g.pose().popMatrix();}
        if(active&&overSwatch&&picker!=null)g.requestCursor(CursorTypes.POINTING_HAND);
    }
}

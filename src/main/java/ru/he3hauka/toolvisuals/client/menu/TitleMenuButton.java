package ru.he3hauka.toolvisuals.client.menu;

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.gui.surface.UiSurface;
import ru.he3hauka.toolvisuals.client.gui.text.UiTextStyle;

public final class TitleMenuButton extends Button {
    private final UiPaint.Icon icon;
    private final float scale;
    private final boolean primary;
    private final Motion hover=new Motion(0);
    private UiPalette palette=new UiPalette(0);
    private float opacity,elapsed;
    private boolean light;
    public TitleMenuButton(Component label,UiPaint.Icon icon,Supplier<String> detail,float scale,boolean primary,Runnable action) {
        super(0,0,1,1,label,button->action.run(),DEFAULT_NARRATION);
        this.icon=icon;this.scale=scale;this.primary=primary;
        if(detail!=null)setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(label.getString()+": "+detail.get())));
    }
    public void frame(UiPalette palette,boolean light,float opacity,float elapsed) { this.palette=palette;this.light=light;this.opacity=opacity;this.elapsed=elapsed; }
    @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial) {
        float over=hover.step(active&&isHoveredOrFocused()?1:0,elapsed,12),a=opacity*(active?1:.4f);
        int x=getX(),y=getY(),w=getWidth(),h=getHeight();
        UiPaint.material(g,primary?UiSurface.PRIMARY:UiSurface.ROW,light,x,y,w,h,scale,a);
        UiPaint.material(g,UiSurface.ACTIVE,light,x,y,w,h,scale,a*over);
        float size=14*scale,iconX=x+12*scale;
        if(icon!=null&&w/scale<58){UiPaint.icon(g,icon,x+(w-size)/2,y+(h-size)/2,size,palette.accentText,a);return;}
        if(icon!=null)UiPaint.icon(g,icon,iconX,y+(h-size)/2,size,primary?palette.accentText:UiPalette.mix(palette.muted,palette.accentText,over),a);
        boolean navigation=icon!=null&&icon!=UiPaint.Icon.RESET;
        float textX=x+(icon==null?10:36)*scale,available=x+w-(navigation?28:11)*scale-textX;
        if(icon==null)UiPaint.centered(g,UiPaint.fit(g,getMessage(),w-12*scale,.9f*scale,UiTextStyle.MENU),x,y,w,h,.9f*scale,palette.text,a,UiTextStyle.MENU);
        else UiPaint.baseline(g,UiPaint.fit(g,getMessage(),available,.9f*scale,UiTextStyle.MENU),textX,UiPaint.textBaseline(g,y,h,.9f*scale,UiTextStyle.MENU),.9f*scale,palette.text,a,UiTextStyle.MENU);
        if(navigation)UiPaint.icon(g,UiPaint.Icon.CHEVRON,x+w-(23-over*1.5f)*scale,y+(h-12*scale)/2,12*scale,
                UiPalette.mix(palette.faint,palette.accentText,over),a);
    }
}

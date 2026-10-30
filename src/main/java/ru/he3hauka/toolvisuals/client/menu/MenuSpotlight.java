package ru.he3hauka.toolvisuals.client.menu;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.RoundedGlow;

final class MenuSpotlight {
    private final Motion x=new Motion(0),y=new Motion(0),strength=new Motion(0);
    private boolean initialized;

    void update(float left,float top,float width,float height,float mouseX,float mouseY,float elapsed) {
        boolean inside=RoundedGlow.contains(left,top,width,height,16,mouseX,mouseY);
        if(inside) {
            if(!initialized) {x.snap(mouseX);y.snap(mouseY);initialized=true;}
            else {x.step(mouseX,elapsed,16);y.step(mouseY,elapsed,16);}
        }
        strength.step(inside?1:0,elapsed,10);
    }

    void draw(GuiGraphicsExtractor g,float left,float top,float width,float height,boolean light,float opacity) {
        float fade=strength.value()*opacity;
        if(fade<.005f)return;
        int color=light?0xffbba0ed:0xffb694f6;
        UiPaint.clippedGlow(g,left+1,top+1,width-2,height-2,15,x.value(),y.value(),72,color,fade*(light?.14f:.12f));
    }
}

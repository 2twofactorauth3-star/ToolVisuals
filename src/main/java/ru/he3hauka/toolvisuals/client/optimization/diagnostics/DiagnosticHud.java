package ru.he3hauka.toolvisuals.client.optimization.diagnostics;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class DiagnosticHud {
    private DiagnosticHud(){}
    public static void draw(GuiGraphicsExtractor graphics){
        var client=Minecraft.getInstance();
        if(!Diagnostics.active()||!client.gui.hud.getDebugOverlay().showDebugScreen())return;
        var sample=Diagnostics.view();int width=Math.min(240,client.getWindow().getGuiScaledWidth()-16),x=8,y=client.getWindow().getGuiScaledHeight()-70;
        if(width<32||y<8)return;
        graphics.fill(x-3,y-3,x+width+3,y+62,0xd0101118);
        var values=sample.milliseconds();int count=Math.min(width,values.length);
        for(int i=0;i<count;i++){double value=values[values.length-count+i];int height=(int)Math.clamp(value/50*34,1,34);graphics.fill(x+i,y+35-height,x+i+1,y+35,value>50?0xfff07a84:value>25?0xffffc76b:0xff9c95ee);}
        graphics.text(client.font,String.format(Locale.ROOT,"Frame %.1fms | p95 %.1f",sample.average(),sample.p95()),x,y+39,0xffeeeeff,false);
        graphics.text(client.font,String.format(Locale.ROOT,"p99 %.1f | >50ms %d",sample.p99(),sample.over50()),x,y+49,0xffc3bfdc,false);
    }
}

package ru.he3hauka.toolvisuals.client.hud.cooldown;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class CooldownHud {
    private record Row(ItemStack item,String title,String time,float progress) {}
    private CooldownHud() {}
    public static void draw(GuiGraphicsExtractor g,UiPalette palette) {
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;
        var entries=Cooldowns.entries(client.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        boolean preview=HudPlacement.samples()||HudPlacement.preview()&&entries.isEmpty();
        if(entries.isEmpty()&&!preview)return;
        int count=preview?2:Math.min(8,entries.size());
        float scale=Math.clamp(client.getWindow().getHeight()/720f,1,2)*config.number(COOLDOWN_SCALE)/client.getWindow().getGuiScale();
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        boolean icons=config.flag(COOLDOWN_ICONS),bars=config.flag(COOLDOWN_BARS);
        var heading=UiI18n.tr("toolvisuals.cooldowns.title");
        float w=UiPaint.textWidth(g,heading.getString(),.82f)+16;
        var rows=new ArrayList<Row>(count);
        for(int i=0;i<(preview?2:entries.size())&&rows.size()<8;i++) {
            var entry=preview?null:entries.get(i);
            ItemStack item=preview?new ItemStack(i==0?Items.ENDER_PEARL:Items.CHORUS_FRUIT):Cooldowns.item(Identifier.parse(entry.group));
            if(item.isEmpty())continue;
            float ticks=preview?(i==0?168:42):entry.remaining;
            float progress=preview?.65f:Math.clamp(ticks/entry.duration,0,1);
            String time=ticks>=1200?HudFormats.duration((int)Math.ceil(ticks),false):String.format(Locale.ROOT,"%.1fs",ticks/20);
            String title=item.getHoverName().getString();
            rows.add(new Row(item,title,time,progress));
            w=Math.max(w,16+(icons?21:0)+UiPaint.textWidth(g,title,.75f)+8+UiPaint.textWidth(g,time,.72f));
        }
        count=rows.size();
        if(count==0){g.pose().popMatrix();return;}
        w=Math.min(240,Math.max(108,w));
        int rowHeight=bars?24:21;
        float h=24+count*rowHeight+3;
        float x=HudFormats.position(g.guiWidth()/scale,w,HudPlacement.value(COOLDOWN_X));
        float y=HudFormats.position(g.guiHeight()/scale,h,HudPlacement.value(COOLDOWN_Y));
        HudPlacement.place(g,HudElement.COOLDOWNS,x,y,w,h);
        UiPaint.rounded(g,x,y,w,h,7,palette.hudPanel);
        UiPaint.baseline(g,heading.getString(),x+8,UiPaint.textBaseline(g,y,24,.82f),.82f,palette.accentText,1);
        for(int i=0;i<count;i++) {
            var entry=rows.get(i);
            float row=y+24+i*rowHeight,tx=x+8;
            if(icons){if(!entry.item.isEmpty())SmoothHudItems.item(g,entry.item,Math.round(tx),Math.round(row+2));tx+=21;}
            float durationWidth=UiPaint.textWidth(g,entry.time,.72f);
            UiPaint.baseline(g,UiPaint.fit(g,Component.literal(entry.title),x+w-16-durationWidth-tx,.75f),tx,
                    UiPaint.textBaseline(g,row,21,.75f),.75f,palette.text,1);
            UiPaint.baseline(g,entry.time,x+w-8-durationWidth,UiPaint.textBaseline(g,row,21,.72f),.72f,palette.muted,1);
            if(bars){
                UiPaint.rounded(g,tx,row+21,x+w-8-tx,2,1,palette.track);
                UiPaint.rounded(g,tx,row+21,(x+w-8-tx)*entry.progress,2,1,palette.accent);
            }
        }
        g.pose().popMatrix();g.pose().popMatrix();
    }
}

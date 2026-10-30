package ru.he3hauka.toolvisuals.client.utility.bossbars;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class BossBars {
    private BossBars(){}
    public static void draw(GuiGraphicsExtractor g,Collection<? extends BossEvent> events) {
        var entries=events.stream().map(e->new BossBarGroups.Entry(e.getName().getString(),color(e.getColor().name()),e.getProgress(),e.getOverlay().name())).toList();drawEntries(g,entries);
    }
    public static void preview(GuiGraphicsExtractor g) {
        drawEntries(g,List.of(new BossBarGroups.Entry("Wither",0xff9674f5,.7f,"PROGRESS"),new BossBarGroups.Entry("Wither",0xff9674f5,.5f,"PROGRESS"),new BossBarGroups.Entry("Ender Dragon",0xffe680d2,.85f,"PROGRESS")));
    }
    private static void drawEntries(GuiGraphicsExtractor g,List<BossBarGroups.Entry> entries) {
        if(entries.isEmpty())return;
        var config=ToolVisualsClient.CONFIG;var client=Minecraft.getInstance();var palette=new UiPalette(config.lightTheme()?1:0);
        var rows=BossBarGroups.rows(entries,config.flag(BOSS_GROUP),(int)config.value(BOSS_LIMIT));
        float scale=Math.clamp(client.getWindow().getHeight()/720f,1,2)*config.number(BOSS_SCALE)/client.getWindow().getGuiScale();
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        float w=Math.min(220,g.guiWidth()/scale-16),h=rows.size()*30-2;
        float x=HudFormats.position(g.guiWidth()/scale,w,HudPlacement.value(BOSS_X)),y=HudFormats.position(g.guiHeight()/scale,h,HudPlacement.value(BOSS_Y));
        HudPlacement.place(g,HudElement.BOSSES,x,y,w,h);
        for(int i=0;i<rows.size();i++) {
            var row=rows.get(i);float top=y+i*30;
            String name=row.title()+(row.count()>1?" ×"+row.count():"");
            UiPaint.rounded(g,x,top,w,28,6,palette.hudPanel);
            UiPaint.centered(g,UiPaint.fit(g,Component.literal(name),w-14,.78f),x,top,w,18,.78f,palette.text,1);
            UiPaint.rounded(g,x+7,top+20,w-14,3,1.5f,palette.track);
            UiPaint.rounded(g,x+7,top+20,(w-14)*row.progress(),3,1.5f,row.color());
        }
        g.pose().popMatrix();g.pose().popMatrix();
    }
    private static int color(String name){return switch(name){case "PINK"->0xffe680d2;case "BLUE"->0xff739cfa;case "RED"->0xfff07788;case "GREEN"->0xff7ddd9e;case "YELLOW"->0xffebd17e;case "WHITE"->0xffe6e4f2;default->0xff9674f5;};}
}

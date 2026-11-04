package ru.he3hauka.toolvisuals.client.potion.ui;

import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.*;
import ru.he3hauka.toolvisuals.client.hud.players.PlayerListPanel;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.potion.tracker.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class BuffTrackerHud {
    private static List<PlayerListPanel.Row> rows=List.of();
    private static long revision=-1,configRevision=-1;
    private BuffTrackerHud(){}
    public static void draw(GuiGraphicsExtractor g,UiPalette palette){
        var config=ToolVisualsClient.CONFIG;if(!config.flag(BUFF_HUD)&&!HudPlacement.preview())return;
        if(revision!=BuffTracker.revision()||configRevision!=config.revision()){
            var next=new ArrayList<PlayerListPanel.Row>();
            for(var entry:BuffTracker.entries()){
                if(!config.flag(entry.player().equals(net.minecraft.client.Minecraft.getInstance().player.getUUID())?BUFF_SELF:BUFF_OTHERS))continue;
                if(next.size()>=(int)config.value(BUFF_LIMIT))break;
                StringJoiner text=new StringJoiner(" · ");int i=0;
                for(var effect:entry.effects()){
                    if(i++>=3){text.add("+"+(entry.effects().size()-3));break;}
                    var type=BuffTracker.effect(effect.id());String name=type==null?effect.id():type.value().getDisplayName().getString();text.add(name+(effect.amplifier()>0?" "+(effect.amplifier()+1):"")+" "+BuffTracker.time(effect.remaining(),effect.estimated()));
                }
                next.add(new PlayerListPanel.Row(entry.name(),text.toString(),0xffbca6ff,true));
            }
            rows=List.copyOf(next);revision=BuffTracker.revision();configRevision=config.revision();
        }
        var visible=rows;
        if(HudPlacement.samples()||HudPlacement.preview()&&visible.isEmpty())visible=List.of(new PlayerListPanel.Row("Player",UiI18n.tr("toolvisuals.buffs.preview").getString(),0xffbca6ff,true));
        if(visible.isEmpty())return;
        PlayerListPanel.draw(g,palette,HudElement.BUFFS,BUFF_SCALE,visible,UiI18n.tr("toolvisuals.buffs.title"),Component.empty(),Integer.toString(visible.size()),16,"toolvisuals.buffs.more",UiPaint.Icon.PLAYER);
    }
}

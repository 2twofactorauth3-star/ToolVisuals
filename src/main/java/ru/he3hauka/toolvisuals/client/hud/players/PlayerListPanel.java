package ru.he3hauka.toolvisuals.client.hud.players;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.joml.Vector2f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;
import ru.he3hauka.toolvisuals.client.utility.privacy.PrivacyMode;

public final class PlayerListPanel {
    public interface Details {
        float rightWidth(GuiGraphicsExtractor graphics);
        float extraHeight();
        float minimumWidth();
        void draw(GuiGraphicsExtractor graphics,UiPalette palette,float x,float y,float width);
    }
    public record Row(String name,String status,int color,boolean active,Details details){
        public Row(String name,String status,int color,boolean active){this(name,status,color,active,null);}
    }
    private static final int HEADER=26,ROW=23;
    private record Style(int limit,float xx,float xy,int guiScale,boolean protect,boolean streamer,String alias,String user,long privacy){}
    private record Measurement(List<Row> rows,Style style,float width){}
    private static final Map<HudElement,Measurement> MEASUREMENTS=new EnumMap<>(HudElement.class);
    private record Hit(int row,HudPlacement.Bounds bounds){}
    private static final Map<HudElement,List<Hit>> HITS=new EnumMap<>(HudElement.class);
    private PlayerListPanel(){}
    public static int rowAt(HudElement element,double x,double y){for(var hit:HITS.getOrDefault(element,List.of()))if(hit.bounds.contains(x,y))return hit.row;return -1;}

    public static void draw(GuiGraphicsExtractor g,UiPalette palette,HudElement element,VisualSetting scaleSetting,
                            List<Row> rows,Component title,Component empty,String count,int nameLimit,String moreKey,UiPaint.Icon icon){
        var client=Minecraft.getInstance();
        boolean clickable=client.gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen;
        var hits=clickable?new ArrayList<Hit>():null;if(!clickable)HITS.remove(element);
        float scale=Math.clamp(client.getWindow().getHeight()/720f,1,2)*ToolVisualsClient.CONFIG.number(scaleSetting)/client.getWindow().getGuiScale();
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        try{
            float screenWidth=g.guiWidth()/scale,screenHeight=g.guiHeight()/scale;
            float column=Math.min(columnWidth(g,element,rows,nameLimit),Math.max(1,screenWidth-32));
            int maxColumns=Math.max(1,(int)((screenWidth-32)/column));
            var layout=PlayerPanelLayout.arrange(rows.stream().map(row->ROW+(row.details==null?0:row.details.extraHeight())).toList(),Math.max(ROW,Math.min(screenHeight-32,screenHeight*.6f)-HEADER-8),maxColumns);
            float w=16+column*layout.columns(),h=HEADER+layout.height()+3;
            float x=HudFormats.position(screenWidth,w,HudPlacement.value(element.xSetting));
            float y=HudFormats.position(screenHeight,h,HudPlacement.value(element.ySetting));
            HudPlacement.place(g,element,x,y,w,h);
            try{
                UiPaint.rounded(g,x,y,w,h,7,palette.hudPanel);
                UiPaint.icon(g,icon,x+8,y+6,14,palette.accentText,1);
                float countWidth=UiPaint.textWidth(g,count,.75f);
                UiPaint.baseline(g,UiPaint.fit(g,title,w-44-countWidth,.86f),x+28,UiPaint.textBaseline(g,y,HEADER,.86f),.86f,palette.text,1);
                UiPaint.baseline(g,count,x+w-8-countWidth,UiPaint.textBaseline(g,y,HEADER,.75f),.75f,palette.muted,1);
                g.fill(Math.round(x+8),Math.round(y+HEADER-1),Math.round(x+w-8),Math.round(y+HEADER),palette.border);
                for(var cell:layout.cells()){
                    float rx=x+8+cell.column()*column,ry=y+HEADER+cell.y();
                    if(cell.index()<0){
                        UiPaint.baseline(g,UiI18n.tr(moreKey,layout.more()).getString(),rx+5,UiPaint.textBaseline(g,ry,ROW,.8f),.8f,palette.muted,1);
                    }else {
                        g.enableScissor(Math.round(rx),Math.round(ry),Math.round(rx+column),Math.round(ry+cell.height()));
                        row(g,palette,rows.get(cell.index()),rx,ry,column,nameLimit);g.disableScissor();
                        if(clickable){var point=g.pose().transformPosition(rx,ry,new Vector2f());hits.add(new Hit(cell.index(),new HudPlacement.Bounds(point.x,point.y,column*g.pose().m00(),cell.height()*g.pose().m11())));}
                    }
                }
                if(clickable)HITS.put(element,List.copyOf(hits));
                if(rows.isEmpty())UiPaint.baseline(g,UiPaint.fit(g,empty,w-16,.76f),x+8,UiPaint.textBaseline(g,y+HEADER,ROW,.76f),.76f,palette.muted,1);
            }finally{g.pose().popMatrix();}
        }finally{g.pose().popMatrix();}
    }

    private static float columnWidth(GuiGraphicsExtractor g,HudElement element,List<Row> rows,int limit){
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;
        var style=new Style(limit,g.pose().m00(),g.pose().m01(),client.getWindow().getGuiScale(),config.active(VisualModule.NAME_PROTECT),
                PrivacyMode.active(),config.protectedName(),client.getUser().getName(),PrivacyMode.revision());
        var previous=MEASUREMENTS.get(element);
        if(previous!=null&&previous.rows==rows&&previous.style.equals(style))return previous.width;
        float width=144;
        for(var row:rows){
            String name=PlayerNames.shorten(NameProtect.text(row.name),limit);
            width=Math.max(width,UiPaint.textWidth(g,name,.86f)+UiPaint.textWidth(g,row.status,.68f)+29+(row.details==null?0:row.details.rightWidth(g)));
            if(row.details!=null)width=Math.max(width,row.details.minimumWidth());
        }
        MEASUREMENTS.put(element,new Measurement(rows,style,width));return width;
    }

    private static void row(GuiGraphicsExtractor g,UiPalette palette,Row row,float x,float y,float width,int limit){
        int color=row.active?(row.color==0?palette.accentText:row.color):palette.muted;
        float statusWidth=UiPaint.textWidth(g,row.status,.68f);
        float detailsWidth=row.details==null?0:row.details.rightWidth(g);
        UiPaint.rounded(g,x+1,y+9,5,5,2.5f,color);
        var name=Component.literal(PlayerNames.shorten(NameProtect.text(row.name),limit));
        UiPaint.baseline(g,UiPaint.fit(g,name,Math.max(0,width-statusWidth-detailsWidth-23),.86f),x+13,UiPaint.textBaseline(g,y,ROW,.86f),.86f,color,1);
        UiPaint.baseline(g,row.status,x+width-5-statusWidth,UiPaint.textBaseline(g,y,ROW,.68f),.68f,palette.muted,1);
        if(row.details!=null)row.details.draw(g,palette,x,y,width);
    }
}

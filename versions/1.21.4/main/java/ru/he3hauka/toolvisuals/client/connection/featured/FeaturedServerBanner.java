package ru.he3hauka.toolvisuals.client.connection.featured;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;

public final class FeaturedServerBanner {
    private static final int EDGE=8,GAP=14,MIN_SIDE_WIDTH=100,STRIP_HEIGHT=14;
    private boolean above;

    public void layout(ServerSelectionList list){
        above=false;
        if(list==null||list.getRowLeft()-GAP-EDGE>=MIN_SIDE_WIDTH||list.getHeight()<72)return;
        above=true;
        list.setY(list.getY()+STRIP_HEIGHT);
        list.setHeight(list.getHeight()-STRIP_HEIGHT);
    }

    public void draw(GuiGraphicsExtractor graphics,Font font,ServerSelectionList list,UiLanguage language){
        if(graphics.guiWidth()<=EDGE*2||list==null||list.children().isEmpty()
                ||!(list.children().getFirst() instanceof ServerSelectionList.OnlineServerEntry entry)
                ||!FeaturedServer.matches(entry.getServerData()))return;
        int top=list.getY(),bottom=top+list.getHeight();
        if(list.getRowBottom(0)<=top||list.getRowTop(0)>=bottom)return;
        var message=FeaturedServerStyle.promotion(language);
        if(above){
            int width=font.width(message);
            float scale=Math.min(1,(graphics.guiWidth()-EDGE*2)/(float)Math.max(1,width));
            graphics.enableScissor(EDGE,top-STRIP_HEIGHT,graphics.guiWidth()-EDGE,top);
            graphics.pose().pushMatrix();
            try{
                graphics.pose().translate(Math.round((graphics.guiWidth()-width*scale)/2f),Math.round(top-STRIP_HEIGHT+(STRIP_HEIGHT-font.lineHeight*scale)/2));
                graphics.pose().scale(scale,scale);
                graphics.text(font,message,0,0,0xffffffff);
            }finally{graphics.pose().popMatrix();graphics.disableScissor();}
            return;
        }
        int right=list.getRowLeft()-GAP,available=right-EDGE;
        if(available<MIN_SIDE_WIDTH)return;
        var lines=font.split(message,available);
        int pitch=font.lineHeight+2,height=lines.size()*pitch-2,y=(list.getRowTop(0)+list.getRowBottom(0))/2-height/2;
        graphics.enableScissor(EDGE,top,right,bottom);
        try{
            for(var line:lines){graphics.text(font,line,right-font.width(line),y,0xffffffff);y+=pitch;}
        }finally{graphics.disableScissor();}
    }
}

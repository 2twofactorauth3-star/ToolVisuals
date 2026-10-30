package ru.he3hauka.toolvisuals.client.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.layout.ChatResizeModel;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.ChatHudAccess;

public final class ChatResizer {
    private static boolean allowed(){return ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.CHAT_RESIZE);}
    private final ChatResizeModel model=new ChatResizeModel();
    private static int previousWidth=-1;
    private static Object world;
    private static ChatResizer current(){var gui=Minecraft.getInstance().gui;return gui!=null&&gui.screen() instanceof ChatHudAccess chat?chat.toolvisuals$chatResizer():null;}
    private static double scale(){return Math.max(.1,Minecraft.getInstance().options.chatScale().get());}
    private static int screenWidth(){return Minecraft.getInstance().getWindow().getGuiScaledWidth();}
    private static int screenHeight(){return Minecraft.getInstance().getWindow().getGuiScaledHeight();}
    public static int width(int vanilla){
        if(!allowed())return vanilla;
        var current=current();double value=ToolVisualsClient.CONFIG.chatSize().width()/100*screenWidth();
        if(current!=null&&current.model.edge().width)value=current.model.size().width()-12*scale();else if(value==0)return vanilla;
        return (int)Math.clamp(Math.round(value),40,Math.max(40,screenWidth()-12*scale()-2));
    }
    public static int height(int vanilla){
        if(!allowed())return vanilla;
        var gui=Minecraft.getInstance().gui;
        if(gui==null||!(gui.screen() instanceof ChatScreen))return vanilla;
        var current=current();double value=ToolVisualsClient.CONFIG.chatSize().height()/100*screenHeight();
        if(current!=null&&current.model.edge().height)value=current.model.size().height();else if(value==0)return vanilla;
        return Math.max(9,(int)(Math.min(value,Math.max(36,screenHeight()-64))/scale()));
    }
    private ChatResizeModel.Box box(){
        var client=Minecraft.getInstance();double scale=scale();
        int contentWidth=width(ChatComponent.getWidth(client.options.chatWidth().get()));
        int contentHeight=height(ChatComponent.getHeight(client.options.chatHeightFocused().get()));
        int lineHeight=(int)(9*(client.options.chatLineSpacing().get()+1));
        double bottom=screenHeight()-40,right=Math.min(screenWidth()-2,contentWidth+12*scale),height=(contentHeight/Math.max(1,lineHeight))*lineHeight*scale;
        return new ChatResizeModel.Box(0,bottom-height,right,bottom);
    }
    public boolean begin(double x,double y){return allowed()&&model.begin(box(),x,y);}
    public boolean dragging(){return model.dragging();}
    public boolean move(double x,double y){
        if(!allowed())return false;
        if(!model.move(x,y,Math.max(80,screenWidth()-2),Math.max(36,screenHeight()-64)))return false;
        refreshWidth(Minecraft.getInstance());return true;
    }
    public void finish(){
        if(!allowed()){model.finish();return;}
        var edge=model.edge();var size=model.finish();if(size==null)return;
        var config=ToolVisualsClient.CONFIG;
        var previous=config.chatSize();
        config.setChatSize(edge.width?Math.max(40,size.width()-12*scale())/Math.max(1,screenWidth())*100:previous.width(),
                edge.height?size.height()/Math.max(1,screenHeight())*100:previous.height());
        refreshWidth(Minecraft.getInstance());
    }
    public static void tick(Minecraft client){
        if(world!=client.level){world=client.level;previousWidth=-1;}
        if(client.level!=null)refreshWidth(client);
    }
    private static void refreshWidth(Minecraft client){
        int value=width(ChatComponent.getWidth(client.options.chatWidth().get()));
        if(value!=previousWidth){previousWidth=value;client.gui.hud.getChat().rescaleChat();}
    }
    public void paint(GuiGraphicsExtractor graphics,double x,double y){
        if(!allowed())return;
        var box=box();var edge=model.dragging()?model.edge():ChatResizeModel.pick(box,x,y);var palette=new UiPalette(ToolVisualsClient.CONFIG.lightTheme()?1:0);
        int right=(int)Math.round(box.right()),top=(int)Math.round(box.top()),bottom=(int)Math.round(box.bottom());
        int color=edge==ChatResizeModel.Edge.NONE?0xc0b2a6cc:palette.accentText;
        if(edge==ChatResizeModel.Edge.TOP||edge==ChatResizeModel.Edge.CORNER)graphics.fill(0,top,right,top+2,color);
        if(edge==ChatResizeModel.Edge.RIGHT||edge==ChatResizeModel.Edge.CORNER)graphics.fill(right-1,top,right+1,bottom,color);
        graphics.fill(right-10,top-1,right+1,top+1,color);
        graphics.fill(right-1,top-1,right+1,top+10,color);
    }
}

package ru.he3hauka.toolvisuals.client.utility.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

public final class ChatTabs {
    private static ChatRules.Tab selected=ChatRules.Tab.ALL;
    private static long revision=Long.MIN_VALUE;
    private static boolean installed;
    private static Object level;
    private ChatTabs(){}
    public static ChatRules.Tab selected(){return selected;}
    public static void select(ChatRules.Tab tab){selected=tab;refresh(Minecraft.getInstance());}
    public static boolean visible(GuiMessage message) {
        var config=ToolVisualsClient.CONFIG;
        boolean privacy=config.active(VisualModule.STREAMER_MODE)&&config.flag(VisualSetting.STREAMER_PRIVATE_CHAT);
        return ChatRules.visible(config.active(VisualModule.CHAT_TABS)?selected:ChatRules.Tab.ALL,message.content().getString(),message.source()!=GuiMessageSource.PLAYER,ToolVisualsClient.UTILITIES.filters(),privacy);
    }
    public static void refresh(Minecraft client){client.gui.hud.getChat().setVisibleMessageFilter(ChatTabs::visible);revision=ToolVisualsClient.CONFIG.revision();installed=true;}
    public static void tick(Minecraft client) {
        if(client.level!=level){level=client.level;selected=ChatRules.Tab.ALL;installed=false;}
        if(!installed||revision!=ToolVisualsClient.CONFIG.revision())refresh(client);
    }
}

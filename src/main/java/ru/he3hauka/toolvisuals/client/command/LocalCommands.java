package ru.he3hauka.toolvisuals.client.command;

import java.util.Locale;

import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;

public final class LocalCommands {
    private LocalCommands(){}
    public static boolean handle(String text){
        String line=text.strip();String[] tokens=line.split("\\s+",3);
        if(!tokens[0].toLowerCase(Locale.ROOT).equals(".toolvisuals"))return false;
        if(tokens.length>=2&&tokens[1].equalsIgnoreCase("gps")){ru.he3hauka.toolvisuals.client.navigation.gps.Gps.command(tokens.length==3?tokens[2]:"");return true;}
        if(tokens.length>=2&&tokens[1].equalsIgnoreCase("log")){ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.command(tokens.length==3?tokens[2]:"");return true;}
        if(tokens.length>=2&&tokens[1].equalsIgnoreCase("keybinds")){ru.he3hauka.toolvisuals.client.keybinds.Keybinds.requestOpen();return true;}
        if(tokens.length>=2&&tokens[1].equalsIgnoreCase("minigames")){ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient.requestOpen();return true;}
        if(tokens.length>=2&&tokens[1].equalsIgnoreCase("music")){ru.he3hauka.toolvisuals.client.music.MusicClient.requestOpen();return true;}
        if(tokens.length>=2&&tokens[1].equalsIgnoreCase("auction"))return ru.he3hauka.toolvisuals.client.auction.AuctionHelper.localChat(line);
        if(tokens.length>=2&&tokens[1].equalsIgnoreCase("presents")) {
            if(tokens.length==3&&tokens[2].equalsIgnoreCase("reset"))ru.he3hauka.toolvisuals.client.presents.ToolPresents.reset();
            else {ru.he3hauka.toolvisuals.client.presents.ToolPresents.recalculate();ChatMessages.notice("presents.usage");}
            return true;
        }
        var client=Minecraft.getInstance();if(client.player!=null)ChatMessages.notice("keybinds.usage");return true;
    }
}

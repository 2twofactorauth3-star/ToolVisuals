package ru.he3hauka.toolvisuals.client.command;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;

public final class LocalCommands {
    private LocalCommands() {}

    public static boolean handle(String text) {
        var invocation = LocalCommandLine.parse(text);
        if (invocation == null) return false;
        var command = invocation.command();
        if (command == null) { ChatMessages.notice("command.usage"); return true; }
        if (command.featureId != null && !ToolVisualsClient.CONFIG.serverPolicy.allows(command.featureId)) {
            ChatMessages.notice("holyworld.unavailable");
            return true;
        }
        String arguments = invocation.arguments();
        switch (command) {
            case HOLYWORLD -> ru.he3hauka.toolvisuals.client.connection.holyworld.HolyWorldClient.status();
            case RPC -> ru.he3hauka.toolvisuals.client.discord.DiscordPresence.diagnostic();
            case GPS -> ru.he3hauka.toolvisuals.client.navigation.gps.Gps.command(arguments);
            case LOG -> ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.command(arguments);
            case KEYBINDS -> ru.he3hauka.toolvisuals.client.keybinds.Keybinds.requestOpen();
            case MINIGAMES -> ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient.requestOpen();
            case MUSIC -> ru.he3hauka.toolvisuals.client.music.MusicClient.requestOpen();
            case AUCTION -> ru.he3hauka.toolvisuals.client.auction.AuctionHelper.command(arguments);
            case PRESENTS -> {
                if (arguments.equalsIgnoreCase("reset")) ru.he3hauka.toolvisuals.client.presents.ToolPresents.reset();
                else { ru.he3hauka.toolvisuals.client.presents.ToolPresents.recalculate(); ChatMessages.notice("presents.usage"); }
            }
        }
        return true;
    }
}

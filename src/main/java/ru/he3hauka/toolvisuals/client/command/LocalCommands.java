package ru.he3hauka.toolvisuals.client.command;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.auction.AuctionHelper;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.connection.holyworld.HolyWorldClient;
import ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog;
import ru.he3hauka.toolvisuals.client.discord.DiscordPresence;
import ru.he3hauka.toolvisuals.client.keybinds.Keybinds;
import ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient;
import ru.he3hauka.toolvisuals.client.music.MusicClient;
import ru.he3hauka.toolvisuals.client.navigation.gps.Gps;
import ru.he3hauka.toolvisuals.client.presents.ToolPresents;

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
            case HOLYWORLD -> HolyWorldClient.status();
            case RPC -> DiscordPresence.diagnostic();
            case GPS -> Gps.command(arguments);
            case LOG -> ModuleLog.command(arguments);
            case KEYBINDS -> Keybinds.requestOpen();
            case MINIGAMES -> MiniGamesClient.requestOpen();
            case MUSIC -> MusicClient.requestOpen();
            case AUCTION -> AuctionHelper.command(arguments);
            case PRESENTS -> {
                if (arguments.equalsIgnoreCase("reset")) ToolPresents.reset();
                else { ToolPresents.recalculate(); ChatMessages.notice("presents.usage"); }
            }
        }
        return true;
    }
}

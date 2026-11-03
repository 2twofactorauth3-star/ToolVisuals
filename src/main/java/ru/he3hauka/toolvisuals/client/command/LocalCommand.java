package ru.he3hauka.toolvisuals.client.command;

import java.util.Locale;

public enum LocalCommand {
    HOLYWORLD(null), RPC(null), GPS("gps"), LOG(null), KEYBINDS("keybinds"),
    MINIGAMES("mini_games"), MUSIC("music_hud"), AUCTION("auction_helper"), PRESENTS("tool_presents");

    public final String id = name().toLowerCase(Locale.ROOT);
    public final String featureId;

    LocalCommand(String featureId) { this.featureId = featureId; }

    public static LocalCommand find(String id) {
        for (var command : values()) if (command.id.equalsIgnoreCase(id)) return command;
        return null;
    }
}

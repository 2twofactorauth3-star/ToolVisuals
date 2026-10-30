package ru.he3hauka.toolvisuals.relay.paper.config;

import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ServerConfigMigrations {
    public static final String REWARD_COMMAND = "hkit give pvp4 %player%";

    private ServerConfigMigrations() {}

    public static boolean apply(YamlConfiguration config) {
        var commands = config.getStringList("reward.commands");
        if (commands.size() != 1) return false;
        String command = commands.getFirst().strip();
        if (command.startsWith("/")) command = command.substring(1);
        if (!command.equals("kit give %player% pvp4")) return false;
        config.set("reward.commands", List.of(REWARD_COMMAND));
        return true;
    }
}

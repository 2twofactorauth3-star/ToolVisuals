package ru.he3hauka.toolvisuals.relay.paper.reward;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public final class RewardSettings {
    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_.-]{1,32}");
    public final boolean enabled;
    public final String id;
    public final long requiredMillis;
    public final List<String> commands;
    public final int flushSeconds;
    public final String usingText, notUsingText;
    public final Map<String, String> messages;

    public RewardSettings(boolean enabled, String id, long requiredSeconds, List<String> commands, int flushSeconds,
                          String usingText, String notUsingText, Map<String, String> messages) {
        if (id == null || !id.matches("[a-zA-Z0-9_.-]{1,64}")) throw new IllegalArgumentException("Invalid reward.id");
        if (requiredSeconds < 0 || requiredSeconds > 31536000) throw new IllegalArgumentException("Invalid reward.required-playtime-seconds");
        if (commands.isEmpty() || commands.size() > 32) throw new IllegalArgumentException("Reward requires 1..32 commands");
        this.commands = commands.stream().map(command -> {
            String value = command.strip();
            if (value.startsWith("/")) value = value.substring(1);
            if (value.isBlank() || value.length() > 512 || value.chars().anyMatch(Character::isISOControl))
                throw new IllegalArgumentException("Invalid reward command");
            return value;
        }).toList();
        if (flushSeconds < 5 || flushSeconds > 300) throw new IllegalArgumentException("Invalid storage-flush-seconds");
        this.enabled = enabled;
        this.id = id.toLowerCase(java.util.Locale.ROOT);
        this.requiredMillis = requiredSeconds * 1000;
        this.flushSeconds = flushSeconds;
        this.usingText = usingText;
        this.notUsingText = notUsingText;
        this.messages = Map.copyOf(messages);
    }

    public List<String> commands(String player, UUID id) {
        if (!PLAYER_NAME.matcher(player).matches()) throw new IllegalArgumentException("Player name is not safe for command substitution");
        return commands.stream().map(command -> command.replace("%player%", player).replace("%uuid%", id.toString())).toList();
    }
}

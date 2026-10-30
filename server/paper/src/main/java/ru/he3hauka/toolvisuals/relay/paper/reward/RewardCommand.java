package ru.he3hauka.toolvisuals.relay.paper.reward;

import java.util.List;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class RewardCommand implements CommandExecutor, TabCompleter {
    private final RewardService rewards;
    private final RewardSettings settings;

    public RewardCommand(RewardService rewards, RewardSettings settings) { this.rewards = rewards; this.settings = settings; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!(sender instanceof Player player)) { message(sender, "players-only", "Эта команда доступна только игроку."); return true; }
        if (arguments.length != 0) { message(sender, "usage", "/toolvisuals"); return true; }
        if (rewards == null) { message(sender, "unavailable", "Хранилище наград недоступно."); return true; }
        rewards.claim(player); return true;
    }

    private void message(CommandSender sender, String key, String fallback) {
        String text = settings == null ? fallback : settings.messages.getOrDefault(key, fallback);
        ((net.kyori.adventure.audience.Audience)sender).sendMessage(ru.he3hauka.toolvisuals.relay.paper.message.ServerMessages.text(text));
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] arguments) { return List.of(); }
}

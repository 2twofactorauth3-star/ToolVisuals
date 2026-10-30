package ru.he3hauka.toolvisuals.relay.paper.placeholder;

import java.util.Locale;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import ru.he3hauka.toolvisuals.relay.paper.presence.ClientStatus;

public final class ToolVisualsExpansion extends PlaceholderExpansion implements AutoCloseable {
    private final JavaPlugin plugin;
    private final ClientStatus clients;
    private final String using, notUsing;

    public ToolVisualsExpansion(JavaPlugin plugin, ClientStatus clients, String using, String notUsing) {
        this.plugin = plugin; this.clients = clients; this.using = using; this.notUsing = notUsing;
    }

    @Override public String getIdentifier() { return "toolvisuals"; }
    @Override public String getAuthor() { return "He3HaukaX"; }
    @Override public String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override public String onRequest(OfflinePlayer player, String parameter) {
        return switch (parameter.toLowerCase(Locale.ROOT)) {
            case "online" -> Integer.toString(clients.online());
            case "using" -> Boolean.toString(player != null && clients.using(player.getUniqueId()));
            case "status" -> player != null && clients.using(player.getUniqueId()) ? using : notUsing;
            default -> null;
        };
    }

    @Override public void close() { unregister(); }
}

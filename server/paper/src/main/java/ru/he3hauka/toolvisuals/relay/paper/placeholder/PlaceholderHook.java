package ru.he3hauka.toolvisuals.relay.paper.placeholder;

import org.bukkit.plugin.java.JavaPlugin;

import ru.he3hauka.toolvisuals.relay.paper.presence.ClientStatus;

public final class PlaceholderHook {
    private PlaceholderHook() {}

    public static AutoCloseable register(JavaPlugin plugin, ClientStatus clients, String using, String notUsing) {
        var expansion = new ToolVisualsExpansion(plugin, clients, using, notUsing);
        if (expansion.register()) return expansion;
        plugin.getLogger().warning("PlaceholderAPI identifier toolvisuals is already registered");
        return () -> {};
    }
}

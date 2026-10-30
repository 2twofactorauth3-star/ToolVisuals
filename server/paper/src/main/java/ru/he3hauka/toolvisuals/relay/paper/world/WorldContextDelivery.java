package ru.he3hauka.toolvisuals.relay.paper.world;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire;

public final class WorldContextDelivery {
    private java.util.UUID previous;
    private String previousChannel;

    public void send(Plugin plugin, Player player, boolean refresh, boolean extended) {
        send(plugin, player, refresh, extended ? 2 : 1);
    }

    public void send(Plugin plugin, Player player, boolean refresh, int version) {
        var world = player.getWorld();
        String channel = version >= 3 ? WorldContextWire.NAMED_CHANNEL : version == 2 ? WorldContextWire.EXTENDED_CHANNEL : WorldContextWire.CHANNEL;
        if (!refresh && world.getUID().equals(previous) && channel.equals(previousChannel)) return;
        String name=WorldContextWire.validName(world.getName())?world.getName():"";
        var state = new WorldContextWire.State(world.getUID(), WorldContextWire.Area.fromName(world.getName()), name);
        player.sendPluginMessage(plugin, channel, version >= 3 ? WorldContextWire.encodeNamed(state)
                : version == 2 ? WorldContextWire.encodeExtended(state) : WorldContextWire.encode(state));
        previous = world.getUID();
        previousChannel = channel;
    }
}

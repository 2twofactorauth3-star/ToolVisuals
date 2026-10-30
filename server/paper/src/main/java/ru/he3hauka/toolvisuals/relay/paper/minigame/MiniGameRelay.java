package ru.he3hauka.toolvisuals.relay.paper.minigame;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import ru.he3hauka.toolvisuals.animation.network.PacketBudget;
import ru.he3hauka.toolvisuals.minigame.network.MiniGameWire;
import ru.he3hauka.toolvisuals.minigame.server.MiniGameHub;

import static ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.*;

public final class MiniGameRelay implements Listener, AutoCloseable {
    private static final class Endpoint {
        final Player player;
        final PacketBudget input = new PacketBudget(8, 4, System.nanoTime());
        final AtomicBoolean scheduled = new AtomicBoolean();
        final LinkedHashMap<String, Message> outgoing = new LinkedHashMap<>();
        Endpoint(Player player) { this.player = player; }
        synchronized boolean admit(long now) { return input.allow(now); }
        synchronized void offer(Message value) {
            String key = value.reply() > 0 ? "reply" : value.code() == Code.MATCH ? "match:" + value.room() : "notice:" + value.room();
            if (!outgoing.containsKey(key) && outgoing.size() == 8) outgoing.remove(outgoing.keySet().iterator().next());
            outgoing.put(key, value);
        }
        synchronized List<Message> drain() { var result = List.copyOf(outgoing.values()); outgoing.clear(); return result; }
    }
    private final JavaPlugin plugin;
    private final MiniGameHub hub = new MiniGameHub();
    private final Map<UUID, Endpoint> endpoints = new ConcurrentHashMap<>();
    private final ScheduledTask expiry;
    private volatile boolean running = true;

    public MiniGameRelay(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        expiry = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> route(hub.expire(System.nanoTime())), 20, 20);
    }

    public void receive(Player player, byte[] bytes) {
        UUID id = player.getUniqueId(); var endpoint = endpoints.get(id);
        if (endpoint == null) synchronized (endpoints) {
            endpoint = endpoints.get(id);
            if (endpoint == null && endpoints.size() < MiniGameHub.MAX_PLAYERS) { endpoint = new Endpoint(player); endpoints.put(id, endpoint); }
        }
        if (!running || endpoint == null || endpoint.player != player || !endpoint.admit(System.nanoTime())) return;
        final Command command;
        try { command = MiniGameWire.command(bytes); } catch (IllegalArgumentException invalid) { return; }
        var expected = endpoint;
        player.getScheduler().run(plugin, task -> {
            if (!running || endpoints.get(id) != expected || !player.isOnline()) return;
            UUID partner = hub.partner(id, command); var target = endpoints.get(partner);
            boolean visible = partner.equals(NONE) || target != null && player.canSee(target.player);
            if (command.action() != Action.HELLO && command.action() != Action.LEAVE && player.isDead()) visible = false;
            route(hub.handle(id, command, visible, System.nanoTime()));
        }, () -> remove(player));
    }

    private void route(List<MiniGameHub.Delivery> values) {
        for (var delivery : values) {
            var endpoint = endpoints.get(delivery.recipient()); if (endpoint == null || !running) continue;
            endpoint.offer(delivery.message()); schedule(delivery.recipient(), endpoint);
        }
    }

    private void schedule(UUID id, Endpoint endpoint) {
        if (!endpoint.scheduled.compareAndSet(false, true)) return;
        var task = endpoint.player.getScheduler().run(plugin, ignored -> {
            try {
                if (!running || endpoints.get(id) != endpoint || !endpoint.player.isOnline()) return;
                for (var message : endpoint.drain()) {
                    if (message.code() == Code.INVITED || message.code() == Code.WAITING || message.code() == Code.MATCH) {
                        UUID other = id.equals(message.first()) ? message.second() : message.first(); var target = endpoints.get(other);
                        if (target == null || !endpoint.player.canSee(target.player)) { route(hub.cancel(id, Code.UNAVAILABLE)); continue; }
                    }
                    if (endpoint.player.getListeningPluginChannels().contains(STATE_CHANNEL)) endpoint.player.sendPluginMessage(plugin, STATE_CHANNEL, MiniGameWire.encode(message));
                }
            } catch (RuntimeException error) { plugin.getLogger().log(java.util.logging.Level.WARNING, "Could not deliver a ToolVisuals minigame update", error); }
            finally {
                endpoint.scheduled.set(false);
                synchronized (endpoint) { if (!endpoint.outgoing.isEmpty() && running && endpoints.get(id) == endpoint) schedule(id, endpoint); }
            }
        }, () -> remove(endpoint.player));
        if (task == null) { endpoint.scheduled.set(false); remove(endpoint.player); }
    }

    private void remove(Player player) {
        UUID id = player.getUniqueId(); var endpoint = endpoints.get(id);
        if (endpoint != null && endpoint.player == player && endpoints.remove(id, endpoint)) route(hub.remove(id));
    }

    @EventHandler public void quit(PlayerQuitEvent event) { remove(event.getPlayer()); }
    @EventHandler public void death(PlayerDeathEvent event) { route(hub.cancel(event.getEntity().getUniqueId(), Code.CLOSED)); }
    @EventHandler public void world(PlayerChangedWorldEvent event) { route(hub.cancel(event.getPlayer().getUniqueId(), Code.CLOSED)); }
    @EventHandler public void respawn(PlayerRespawnEvent event) { route(hub.cancel(event.getPlayer().getUniqueId(), Code.CLOSED)); }
    @EventHandler public void hidden(PlayerHideEntityEvent event) {
        if (event.getEntity() instanceof Player target && target.getUniqueId().equals(hub.partner(event.getPlayer().getUniqueId())))
            route(hub.cancel(event.getPlayer().getUniqueId(), Code.UNAVAILABLE));
    }
    @Override public void close() { running = false; expiry.cancel(); endpoints.clear(); hub.clear(); HandlerList.unregisterAll(this); }
}

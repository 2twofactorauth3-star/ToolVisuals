package ru.he3hauka.toolvisuals.relay.paper.reward;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.he3hauka.toolvisuals.relay.paper.config.ServerConfiguration;
import ru.he3hauka.toolvisuals.relay.paper.presence.ClientStatus;
import ru.he3hauka.toolvisuals.relay.paper.storage.RewardStorage;
import ru.he3hauka.toolvisuals.relay.paper.storage.StorageWorker;

public final class RewardService implements AutoCloseable {
    private final JavaPlugin plugin;
    private final ClientStatus clients;
    private final RewardSettings settings;
    private final StorageWorker worker;
    private final RewardDelivery delivery;
    private final Map<UUID, PlaytimeAccount> accounts = new ConcurrentHashMap<>();
    private final AtomicBoolean flushing = new AtomicBoolean();
    private final ScheduledTask autosave;
    private volatile boolean closing;

    public RewardService(JavaPlugin plugin, ClientStatus clients, ServerConfiguration config) {
        this.plugin = plugin; this.clients = clients; settings = config.reward;
        worker = new StorageWorker(config.storage, plugin.getLogger());
        delivery = new RewardDelivery(plugin, clients);
        autosave = plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> flush(), settings.flushSeconds, settings.flushSeconds, TimeUnit.SECONDS);
    }

    public void open(UUID player, long epoch, String name) {
        if (closing) return;
        PlaytimeAccount account;
        synchronized (accounts) {
            account = accounts.get(player);
            if (account == null && accounts.size() < 4096) { account = new PlaytimeAccount(player); accounts.put(player, account); }
            if (account != null) account.connect(epoch, name, System.nanoTime());
        }
        if (account != null) load(account);
    }

    public void observe(UUID player, long epoch, boolean using) {
        var account = accounts.get(player);
        if (account != null) { account.activity(epoch, using, System.nanoTime()); load(account); }
    }

    public void disconnect(UUID player, long epoch) {
        var account = accounts.get(player);
        if (account != null) account.disconnect(epoch, System.nanoTime());
    }

    private void load(PlaytimeAccount account) {
        if (closing || worker.state() != StorageWorker.State.READY || !account.beginLoad()) return;
        if (!worker.execute(storage -> {
            try { account.load(storage.load(account.player)); }
            catch (Exception error) { account.loadFailed(); worker.error("Could not load ToolVisuals playtime", error); }
        }, false)) account.loadFailed();
    }

    private void flush() {
        if (closing || !flushing.compareAndSet(false, true)) return;
        if (!worker.execute(storage -> {
            try { save(storage, false); }
            finally { flushing.set(false); }
        }, false)) flushing.set(false);
    }

    private void save(RewardStorage storage, boolean finalSave) throws Exception {
        var snapshots = new HashMap<PlaytimeAccount, Long>();
        var totals = new HashMap<UUID, Long>();
        long now = System.nanoTime();
        for (var account : accounts.values()) {
            if (finalSave && !account.loaded()) account.load(storage.load(account.player));
            long total = account.dirty(now);
            if (total >= 0) { snapshots.put(account, total); totals.put(account.player, total); }
        }
        storage.save(totals);
        snapshots.forEach(PlaytimeAccount::written);
        synchronized (accounts) {
            for (var account : accounts.values()) if (account.removable(System.nanoTime())) accounts.remove(account.player, account);
        }
    }

    public void claim(Player player) {
        UUID id = player.getUniqueId();
        if (!player.hasPermission("toolvisuals.reward")) { message(player, "no-permission"); return; }
        if (!settings.enabled) { message(player, "disabled"); return; }
        if (!clients.using(id)) { message(player, "requires-client"); return; }
        if (closing || worker.state() == StorageWorker.State.FAILED || worker.state() == StorageWorker.State.CLOSED) { message(player, "unavailable"); return; }
        var account = accounts.get(id);
        if (account == null) { message(player, "unavailable"); return; }
        if (!account.loaded()) { load(account); message(player, "loading"); return; }
        if (account.claimed(settings.id)) { message(player, "already-claimed"); return; }
        if (account.blocked(settings.id)) { message(player, "review-required"); return; }
        long total = account.total(System.nanoTime());
        if (total < settings.requiredMillis) { message(player, "remaining", "%time%", duration(settings.requiredMillis - total)); return; }
        if (!account.beginClaim(System.nanoTime())) { message(player, "busy"); return; }
        final java.util.List<String> commands;
        try { commands = settings.commands(player.getName(), id); }
        catch (IllegalArgumentException error) { account.endClaim(); message(player, "unavailable"); return; }
        long epoch = account.epoch(); UUID claim = UUID.randomUUID();
        if (!worker.execute(storage -> {
            if (closing) { account.endClaim(); return; }
            try {
                var result = storage.reserve(id, settings.id, claim, total, settings.requiredMillis, System.currentTimeMillis());
                account.written(total);
                if (result != RewardStorage.Claim.RESERVED) {
                    account.endClaim();
                    if (result == RewardStorage.Claim.ALREADY_CLAIMED) account.claimed(settings.id, false);
                    if (result == RewardStorage.Claim.REVIEW_REQUIRED) account.claimed(settings.id, true);
                    message(player, result == RewardStorage.Claim.ALREADY_CLAIMED ? "already-claimed" : result == RewardStorage.Claim.REVIEW_REQUIRED ? "review-required" : "loading");
                    return;
                }
                if (closing || !clients.current(id, epoch)) { storage.release(id, settings.id, claim); account.endClaim(); return; }
                if (!storage.dispatching(id, settings.id, claim)) throw new IllegalStateException("Reserved reward changed before dispatch");
                if (!delivery.offer(id, epoch, commands, value -> completed(player, account, claim, value))) {
                    storage.release(id, settings.id, claim); account.endClaim(); message(player, "busy");
                }
            } catch (Exception error) {
                account.endClaim(); worker.error("Reward claim requires verification: " + claim + " for " + id, error);
                message(player, "delivery-failed", "%claim%", claim.toString());
            }
        }, false)) { account.endClaim(); message(player, "unavailable"); return; }
        message(player, "processing");
    }

    private void completed(Player player, PlaytimeAccount account, UUID claim, RewardDelivery.Result result) {
        if (!worker.execute(storage -> {
            try {
                if (result.attempted() == 0) storage.release(account.player, settings.id, claim);
                else {
                    storage.complete(account.player, settings.id, claim, result.completed(), result.success());
                    account.claimed(settings.id, !result.success());
                }
                message(player, result.success() ? "received" : result.attempted() == 0 ? "requires-client" : "delivery-failed", "%claim%", claim.toString());
                if (!result.success() && result.attempted() > 0) plugin.getLogger().warning("Reward " + claim + " requires manual review; commands completed=" + result.completed());
            } catch (Exception error) {
                account.claimed(settings.id, true); worker.error("Could not finalize reward " + claim, error);
                message(player, "delivery-failed", "%claim%", claim.toString());
            } finally { account.endClaim(); }
        }, true)) {
            account.claimed(settings.id, true); account.endClaim();
            plugin.getLogger().severe("Reward " + claim + " completion was not persisted; inspect it before granting another reward");
            message(player, "delivery-failed", "%claim%", claim.toString());
        }
    }

    private void message(Player player, String key, String... replacements) {
        if (closing || !plugin.isEnabled()) return;
        String text = settings.messages.getOrDefault(key, key);
        for (int index = 0; index + 1 < replacements.length; index += 2) text = text.replace(replacements[index], replacements[index + 1]);
        var message = ru.he3hauka.toolvisuals.relay.paper.message.ServerMessages.text(text);
        try { player.getScheduler().run(plugin, task -> ((net.kyori.adventure.audience.Audience)player).sendMessage(message), null); }
        catch (RuntimeException ignored) {}
    }

    private static String duration(long millis) {
        long seconds = (millis + 999) / 1000;
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    @Override public void close() {
        closing = true; autosave.cancel(); delivery.close();
        long now = System.nanoTime();
        for (var account : accounts.values()) account.disconnect(account.epoch(), now);
        worker.close(storage -> save(storage, true));
        if (worker.state() == StorageWorker.State.CLOSED) accounts.clear();
    }
}

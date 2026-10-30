package ru.he3hauka.toolvisuals.relay.paper.reward;

import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.bukkit.plugin.java.JavaPlugin;

import ru.he3hauka.toolvisuals.relay.paper.presence.ClientStatus;

public final class RewardDelivery implements AutoCloseable {
    public record Result(int completed, int attempted, boolean success) {}
    private static final class Job {
        final UUID player;
        final long epoch;
        final List<String> commands;
        final Consumer<Result> finish;
        int completed, attempted;
        Job(UUID player, long epoch, List<String> commands, Consumer<Result> finish) {
            this.player = player; this.epoch = epoch; this.commands = commands; this.finish = finish;
        }
    }
    private final JavaPlugin plugin;
    private final ClientStatus clients;
    private final ArrayDeque<Job> waiting = new ArrayDeque<>();
    private final AtomicBoolean scheduled = new AtomicBoolean();
    private volatile boolean closed;
    private int active;

    public RewardDelivery(JavaPlugin plugin, ClientStatus clients) { this.plugin = plugin; this.clients = clients; }

    public boolean offer(UUID player, long epoch, List<String> commands, Consumer<Result> finish) {
        synchronized (waiting) {
            if (closed || active >= 128) return false;
            active++;
            waiting.addLast(new Job(player, epoch, commands, finish));
        }
        schedule(); return true;
    }

    private void schedule() {
        if (closed || !scheduled.compareAndSet(false, true)) return;
        try { plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> drain(), 1); }
        catch (RuntimeException error) { scheduled.set(false); close(); }
    }

    private void drain() {
        for (int budget = 0; budget < 4 && !closed; budget++) {
            Job job;
            synchronized (waiting) { job = waiting.pollFirst(); }
            if (job == null) break;
            boolean success = false;
            if (clients.current(job.player, job.epoch)) {
                job.attempted++;
                try { success = plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), job.commands.get(job.completed)); }
                catch (RuntimeException error) { plugin.getLogger().warning("A configured reward command failed for " + job.player + " at command " + job.completed); }
            }
            if (success) job.completed++;
            if (!success || job.completed == job.commands.size() || closed) finish(job, success && !closed && job.completed == job.commands.size());
            else synchronized (waiting) { waiting.addLast(job); }
        }
        scheduled.set(false);
        synchronized (waiting) { if (!waiting.isEmpty()) schedule(); }
    }

    private void finish(Job job, boolean success) {
        synchronized (waiting) { active--; }
        job.finish.accept(new Result(job.completed, job.attempted, success));
    }

    @Override public void close() {
        synchronized (waiting) {
            if (closed) return;
            closed = true;
            Job job;
            while ((job = waiting.pollFirst()) != null) finish(job, false);
        }
    }
}

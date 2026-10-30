package ru.he3hauka.toolvisuals.relay.paper.storage;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StorageWorker implements AutoCloseable {
    @FunctionalInterface public interface Work { void run(RewardStorage storage) throws Exception; }
    public enum State { STARTING, READY, FAILED, CLOSED }
    private final ThreadPoolExecutor executor;
    private final AtomicInteger pending = new AtomicInteger();
    private final Logger logger;
    private volatile State state = State.STARTING;
    private volatile boolean closing;
    private RewardStorage storage;
    private long lastError = Long.MIN_VALUE;

    public StorageWorker(StorageSettings settings, Logger logger) {
        this.logger = logger;
        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(2050), task -> {
            var thread = new Thread(task, "ToolVisuals-storage"); thread.setDaemon(true); return thread;
        }, new ThreadPoolExecutor.AbortPolicy());
        executor.execute(() -> {
            try { storage = new RewardStorage(settings); state = State.READY; }
            catch (Exception | LinkageError error) { state = State.FAILED; logger.log(Level.SEVERE, "Reward storage could not start; rewards are disabled and existing data are preserved", error); }
        });
    }

    public State state() { return state; }

    public synchronized boolean execute(Work work, boolean critical) {
        if (state != State.READY || executor.isShutdown() || closing && !critical || pending.get() >= 2048) return false;
        pending.incrementAndGet();
        try {
            executor.execute(() -> {
                try { if (!closing || critical) work.run(storage); }
                catch (Exception error) { error("Reward storage operation failed", error); }
                finally { pending.decrementAndGet(); }
            });
            return true;
        } catch (RejectedExecutionException error) { pending.decrementAndGet(); return false; }
    }

    public synchronized void error(String message, Throwable error) {
        long now = System.nanoTime();
        if (lastError != Long.MIN_VALUE && now - lastError < 30000000000L) return;
        lastError = now; logger.log(Level.SEVERE, message, error);
    }

    public void close(Work flush) {
        synchronized (this) {
            if (closing) return;
            closing = true;
            executor.execute(() -> {
                try { if (storage != null) flush.run(storage); }
                catch (Exception error) { logger.log(Level.SEVERE, "Final playtime flush failed; the previous database remains intact", error); }
                finally { if (storage != null) storage.close(); state = State.CLOSED; }
            });
            executor.shutdown();
        }
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) logger.severe("Storage shutdown has not completed within 30 seconds; inspect the database before restarting rewards");
        } catch (InterruptedException error) { Thread.currentThread().interrupt(); }
    }

    @Override public void close() { close(storage -> {}); }
}

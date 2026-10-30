package ru.he3hauka.toolvisuals.relay.paper.reward;

import java.util.UUID;

public final class PlaytimeAccount {
    public final UUID player;
    private long epoch, checkpoint = Long.MIN_VALUE, pendingNanos, storedMillis, writtenMillis;
    private boolean connected, using, loaded, loading, claiming;
    private String name;
    private String completedReward, blockedReward;
    private long nextClaim = Long.MIN_VALUE;

    public PlaytimeAccount(UUID player) { this.player = player; }

    private void accrue(long now) {
        if (connected && using && now > checkpoint) pendingNanos = Math.addExact(pendingNanos, now - checkpoint);
        checkpoint = Math.max(checkpoint, now);
    }

    public synchronized void connect(long epoch, String name, long now) {
        if (epoch <= this.epoch) return;
        accrue(now);
        this.epoch = epoch; this.name = name; connected = true; using = false;
    }

    public synchronized void activity(long epoch, boolean using, long now) {
        if (this.epoch != epoch || !connected) return;
        accrue(now); this.using = using;
    }

    public synchronized void disconnect(long epoch, long now) {
        if (this.epoch != epoch) return;
        accrue(now); connected = using = false;
    }

    public synchronized void load(long millis) {
        if (loaded) return;
        storedMillis = Math.max(0, millis); writtenMillis = storedMillis; loaded = true; loading = false;
    }

    public synchronized boolean beginLoad() { if (loaded || loading) return false; loading = true; return true; }
    public synchronized void loadFailed() { loading = false; }
    public synchronized boolean loaded() { return loaded; }
    public synchronized boolean connected() { return connected; }
    public synchronized long epoch() { return epoch; }
    public synchronized String name() { return name; }
    public synchronized long total(long now) { accrue(now); return Math.addExact(storedMillis, pendingNanos / 1000000); }
    public synchronized long dirty(long now) { long total = total(now); return loaded && total > writtenMillis ? total : -1; }
    public synchronized void written(long millis) { writtenMillis = Math.max(writtenMillis, millis); }
    public synchronized boolean beginClaim(long now) {
        if (claiming || !loaded || !connected || !using || now < nextClaim) return false;
        nextClaim = now + 1000000000L; claiming = true; return true;
    }
    public synchronized void endClaim() { claiming = false; }
    public synchronized boolean claiming() { return claiming; }
    public synchronized boolean claimed(String reward) { return reward.equals(completedReward); }
    public synchronized boolean blocked(String reward) { return reward.equals(blockedReward); }
    public synchronized void claimed(String reward, boolean review) { if (review) blockedReward = reward; else completedReward = reward; }
    public synchronized boolean removable(long now) { return !connected && !claiming && loaded && dirty(now) < 0; }
}

package ru.he3hauka.toolvisuals.relay.paper.presence;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ClientStatus {
    private record Session(long epoch, boolean using) {}
    private final ConcurrentHashMap<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger online = new AtomicInteger();

    public void open(UUID player, long epoch) {
        sessions.compute(player, (id, previous) -> {
            if (previous != null && previous.epoch >= epoch) return previous;
            if (previous != null && previous.using) online.decrementAndGet();
            return new Session(epoch, false);
        });
    }

    public void update(UUID player, long epoch, boolean using) {
        sessions.computeIfPresent(player, (id, previous) -> {
            if (previous.epoch != epoch || previous.using == using) return previous;
            online.addAndGet(using ? 1 : -1);
            return new Session(epoch, using);
        });
    }

    public void close(UUID player, long epoch) {
        sessions.computeIfPresent(player, (id, previous) -> {
            if (previous.epoch != epoch) return previous;
            if (previous.using) online.decrementAndGet();
            return null;
        });
    }

    public boolean using(UUID player) {
        var session = sessions.get(player);
        return session != null && session.using;
    }

    public boolean current(UUID player, long epoch) {
        var session = sessions.get(player);
        return session != null && session.epoch == epoch && session.using;
    }

    public int online() { return online.get(); }
}

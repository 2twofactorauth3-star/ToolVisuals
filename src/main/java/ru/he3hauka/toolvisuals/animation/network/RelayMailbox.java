package ru.he3hauka.toolvisuals.animation.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RelayMailbox<T> {
    private final int capacity;
    private final Map<UUID, T> pending = new LinkedHashMap<>();
    private boolean scheduled, closed;

    public RelayMailbox(int capacity) {
        if (capacity < 1 || capacity > 64) throw new IllegalArgumentException("Invalid relay mailbox capacity");
        this.capacity = capacity;
    }

    public synchronized boolean offer(UUID actor, T value) {
        if (closed) return false;
        pending.put(Objects.requireNonNull(actor), Objects.requireNonNull(value));
        if (pending.size() > capacity) pending.remove(pending.keySet().iterator().next());
        if (scheduled) return false;
        scheduled = true;
        return true;
    }

    public synchronized List<T> drain() {
        var values = List.copyOf(pending.values());
        pending.clear();
        scheduled = false;
        return values;
    }

    public synchronized void close() {
        closed = true;
        pending.clear();
    }
}

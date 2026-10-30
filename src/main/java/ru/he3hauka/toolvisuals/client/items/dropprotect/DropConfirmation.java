package ru.he3hauka.toolvisuals.client.items.dropprotect;

import java.util.Objects;

public final class DropConfirmation<K> {
    public record Decision(boolean allowed, int remaining, boolean notifyPlayer) {}

    private K pending;
    private long started, window, lastInput;
    private int required, confirmed;

    public K pending() { return pending; }

    public Decision attempt(K key, long input, long now, int confirmations, long timeout) {
        if (input <= lastInput) return new Decision(false, required - confirmed, false);
        lastInput = input;
        if (!Objects.equals(pending, key) || expired(now) || required != confirmations || window != timeout) {
            pending = Objects.requireNonNull(key);
            started = now;
            window = timeout;
            required = confirmations;
            confirmed = 0;
            return new Decision(false, required, true);
        }
        if (++confirmed < required) return new Decision(false, required - confirmed, true);
        clear();
        return new Decision(true, 0, false);
    }

    public boolean expired(long now) { return pending != null && now - started >= window; }

    public void clear() {
        pending = null;
        required = confirmed = 0;
    }
}

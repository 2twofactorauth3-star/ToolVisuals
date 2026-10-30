package ru.he3hauka.toolvisuals.client.connection.holyworld;

import java.util.Set;
import java.util.UUID;

import ru.he3hauka.toolvisuals.client.connection.policy.ServerFeaturePolicy;

public final class HolyWorldSession {
    private static final long RETRY = 5_000_000_000L, REFRESH = 60_000_000_000L;
    private final ServerFeaturePolicy policy;
    private Object connection;
    private String pending;
    private long due, window;
    private int attempts, received, receivedBytes;
    private boolean holyWorld, advertised, confirmed;

    public HolyWorldSession(ServerFeaturePolicy policy) {
        this.policy = policy;
    }

    public void begin(Object connection, String address) {
        if (this.connection == connection) return;
        reset();
        this.connection = connection;
        holyWorld = HolyWorldHosts.matches(address);
        policy.update(holyWorld, Set.of());
    }

    public void advertise(boolean supported) {
        if (!supported || advertised) return;
        advertised = true;
        holyWorld = true;
        policy.update(true, policy.blocked());
        if (pending == null) { due = 0; attempts = 0; }
    }

    public byte[] request(long now) {
        if (connection == null || now < due) return null;
        if (pending != null && attempts >= 3) {
            pending = null;
            due = holyWorld || advertised ? now + REFRESH : Long.MAX_VALUE;
            return null;
        }
        if (pending == null) {
            pending = UUID.randomUUID().toString();
            attempts = 0;
        }
        attempts++;
        due = now + RETRY;
        return HolyWorldProtocol.request(pending);
    }

    public boolean admit(Object connection, int bytes, long now) {
        if (this.connection != connection || pending == null || bytes < 1 || bytes > HolyWorldProtocol.MAX_BYTES) return false;
        if (received == 0 || now - window >= 10_000_000_000L) {
            received = receivedBytes = 0;
            window = now;
        }
        if (received >= 8 || receivedBytes + bytes > 131_072) return false;
        received++;
        receivedBytes += bytes;
        return true;
    }

    public boolean accept(Object connection, HolyWorldProtocol.Response response, long now) {
        if (!pending(connection, response.id())) return false;
        if (!response.ok()) return false;
        holyWorld = confirmed = true;
        policy.update(true, response.blocked());
        pending = null;
        attempts = 0;
        due = now + REFRESH;
        return true;
    }

    public boolean pending(Object connection, String id) {
        return this.connection == connection && pending != null && pending.equals(id);
    }

    public boolean confirmed() {
        return confirmed;
    }

    public boolean protectedServer() {
        return holyWorld;
    }

    public void reset() {
        connection = null;
        pending = null;
        due = window = 0;
        attempts = received = receivedBytes = 0;
        holyWorld = advertised = confirmed = false;
        policy.clear();
    }
}

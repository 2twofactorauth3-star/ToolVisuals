package ru.he3hauka.toolvisuals.client.replay.capture;

public final class ReplayStartGate {
    private Object attempted;

    public synchronized boolean begin(boolean enabled, Object connection) {
        if (!enabled) {
            attempted = null;
            return false;
        }
        if (connection == null || attempted == connection) return false;
        attempted = connection;
        return true;
    }

    public synchronized void started(Object connection) {
        attempted = connection;
    }
}

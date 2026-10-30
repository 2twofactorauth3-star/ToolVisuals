package ru.he3hauka.toolvisuals.client.input;

public final class VirtualKeyScope implements AutoCloseable {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private final Thread owner = Thread.currentThread();
    private boolean closed;

    private VirtualKeyScope() { DEPTH.set(DEPTH.get() + 1); }

    public static VirtualKeyScope physicalUpdate() { return new VirtualKeyScope(); }
    public static boolean virtualAllowed() { return DEPTH.get() == 0; }

    @Override public void close() {
        if (Thread.currentThread() != owner) throw new IllegalStateException("Key scope belongs to another thread");
        if (closed) return;
        int depth = DEPTH.get() - 1;
        if (depth == 0) DEPTH.remove(); else DEPTH.set(depth);
        closed = true;
    }
}

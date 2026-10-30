package ru.he3hauka.toolvisuals.client.replay.io;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

public final class ReplayStream implements AutoCloseable {
    private final ArrayBlockingQueue<ReplayFrame> frames = new ArrayBlockingQueue<>(128);
    private final Semaphore bytes = new Semaphore(512);
    private final Thread worker;
    private volatile Throwable failure;
    private volatile boolean ended, closed;

    public ReplayStream(ReplaySnapshot snapshot) {
        worker = new Thread(() -> read(snapshot), "ToolVisuals replay reader"); worker.setDaemon(true); worker.start();
    }

    private void read(ReplaySnapshot snapshot) {
        try (var reader = new ReplayReader(snapshot)) {
            while (!closed) {
                var frame = reader.next();
                if (frame == null) break;
                bytes.acquire(cost(frame)); frames.put(frame);
            }
        } catch (Exception error) { if (!closed) {failure = error;ClientErrors.report("Read replay stream",error);} }
        finally { ended = true; }
    }

    private static int cost(ReplayFrame frame) { return Math.max(1, (frame.size + 65535) / 65536); }
    public ReplayFrame poll() { var frame = frames.poll(); if (frame != null) bytes.release(cost(frame)); return frame; }
    public Throwable failure() { return failure; }
    public boolean ended() { return ended && frames.isEmpty(); }
    @Override public void close() { closed = true; worker.interrupt(); frames.clear(); }
}

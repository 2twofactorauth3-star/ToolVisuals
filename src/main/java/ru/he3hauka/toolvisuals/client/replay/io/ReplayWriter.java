package ru.he3hauka.toolvisuals.client.replay.io;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;

public final class ReplayWriter implements AutoCloseable {
    private sealed interface Work permits Frame, Snapshot, Finish {}
    private record Frame(ReplayFrame value) implements Work {}
    private record Snapshot(CompletableFuture<ReplaySnapshot> result) implements Work {}
    private record Finish() implements Work {}
    private static final long QUEUED_BYTE_LIMIT = 64L * 1024 * 1024;
    public final Path path;
    public final ReplayHeader header;
    private final long byteLimit;
    private final ArrayBlockingQueue<Work> queue = new ArrayBlockingQueue<>(2050);
    private final AtomicLong queuedBytes = new AtomicLong();
    private final CompletableFuture<ReplaySnapshot> finished = new CompletableFuture<>();
    private CompletableFuture<ReplaySnapshot> snapshot;
    private int queuedFrames;
    private boolean closing;
    private volatile Throwable failure;

    public ReplayWriter(Path path, ReplayHeader header, long byteLimit) {
        if (byteLimit < 1024 * 1024 || byteLimit > ReplayFormat.MAX_FILE_BYTES) throw new IllegalArgumentException("Invalid replay file budget");
        this.path = path.toAbsolutePath().normalize(); this.header = header; this.byteLimit = byteLimit;
        var thread = new Thread(this::run, "ToolVisuals-replay-writer"); thread.setDaemon(true); thread.start();
    }

    public synchronized boolean offer(ReplayFrame frame) {
        if (closing || failure != null || frame.kind == ReplayFrame.Kind.END) return false;
        if (queuedFrames >= 2048 || queuedBytes.get() + frame.size > QUEUED_BYTE_LIMIT) {
            failure = new IOException("Replay writer backlog exceeded its budget"); close(); return false;
        }
        queuedFrames++; queuedBytes.addAndGet(frame.size);
        if (!queue.offer(new Frame(frame))) throw new IllegalStateException("Reserved replay queue capacity was lost");
        return true;
    }

    public synchronized CompletableFuture<ReplaySnapshot> snapshot() {
        if (closing) return finished;
        if (snapshot == null || snapshot.isDone()) {
            snapshot = new CompletableFuture<>();
            if (!queue.offer(new Snapshot(snapshot))) snapshot.completeExceptionally(new IOException("Replay snapshot queue is full"));
        }
        return snapshot;
    }

    public synchronized CompletableFuture<ReplaySnapshot> finish() {
        if (!closing) { closing = true; if (!queue.offer(new Finish())) finished.completeExceptionally(new IOException("Replay finalization queue is full")); }
        return finished;
    }

    public Throwable failure() { return failure; }
    @Override public void close() { finish(); }

    private void run() {
        var deflater = new Deflater(Deflater.BEST_SPEED);
        try {
            Files.createDirectories(path.getParent());
            try (var channel = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                 var output = new DataOutputStream(new BufferedOutputStream(Channels.newOutputStream(channel), 65536))) {
                ReplayFormat.writeHeader(output, header); output.flush();
                long size = channel.position(), duration = 0, count = 0;
                while (true) {
                    Work work = queue.take();
                    if (work instanceof Frame item) {
                        var frame = item.value;
                        try {
                            if (count >= ReplayFormat.MAX_FRAMES || size + ReplayFormat.FRAME_HEADER_BYTES + frame.size + ReplayFormat.FRAME_HEADER_BYTES > byteLimit)
                                throw new IOException("Replay reached its recording limit");
                            duration = Math.max(duration, frame.millis);
                            size += ReplayFormat.writeFrame(output, frame, duration, deflater); count++;
                        } finally { synchronized (this) { queuedFrames--; queuedBytes.addAndGet(-frame.size); } }
                    } else if (work instanceof Snapshot item) {
                        output.flush(); channel.force(false);
                        item.result.complete(new ReplaySnapshot(path, header, channel.position(), duration, count, false));
                    } else {
                        ReplayFormat.writeFrame(output, ReplayFrame.takeOwnership(ReplayFrame.Kind.END, duration, new byte[0]), duration, deflater);
                        output.flush(); channel.force(false);
                        finished.complete(new ReplaySnapshot(path, header, channel.position(), duration, count, true));
                        break;
                    }
                }
            }
        } catch (Exception error) {
            ClientErrors.report("Write replay recording",error);
            failure = error; finished.completeExceptionally(error);
            synchronized (this) { if (snapshot != null) snapshot.completeExceptionally(error); closing = true; queue.clear(); queuedFrames = 0; queuedBytes.set(0); }
        } finally { deflater.end(); }
    }
}

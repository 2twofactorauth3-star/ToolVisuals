package ru.he3hauka.toolvisuals.client.replay.io;

import java.nio.file.Path;
import java.util.Objects;

public final class ReplaySnapshot {
    public final Path path;
    public final ReplayHeader header;
    public final long length, durationMillis, frames;
    public final boolean complete;

    public ReplaySnapshot(Path path, ReplayHeader header, long length, long durationMillis, long frames, boolean complete) {
        if (length <= 0 || length > ReplayFormat.MAX_FILE_BYTES || durationMillis < 0 || durationMillis > ReplayFormat.MAX_DURATION_MILLIS
                || frames < 0 || frames > ReplayFormat.MAX_FRAMES) throw new IllegalArgumentException("Invalid replay snapshot");
        this.path = Objects.requireNonNull(path); this.header = Objects.requireNonNull(header);
        this.length = length; this.durationMillis = durationMillis; this.frames = frames; this.complete = complete;
    }
}

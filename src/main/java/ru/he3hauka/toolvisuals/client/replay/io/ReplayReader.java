package ru.he3hauka.toolvisuals.client.replay.io;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.Inflater;

import ru.he3hauka.toolvisuals.client.storage.BoundedInputStream;

public final class ReplayReader implements AutoCloseable {
    public final ReplayHeader header;
    private final DataInputStream input;
    private final Inflater inflater = new Inflater();
    private long time, frames;
    private boolean ended;

    public ReplayReader(ReplaySnapshot snapshot) throws IOException {
        if (Files.isSymbolicLink(snapshot.path) || snapshot.length > Files.size(snapshot.path)) throw new IOException("Replay snapshot is unavailable");
        input = new DataInputStream(new BufferedInputStream(BoundedInputStream.prefix(Files.newInputStream(snapshot.path), snapshot.length), 65536));
        try {
            header = ReplayFormat.readHeader(input);
            if (!header.recording.equals(snapshot.header.recording) || !header.player.equals(snapshot.header.player)
                    || header.protocol != snapshot.header.protocol || !header.gameVersion.equals(snapshot.header.gameVersion)) throw new IOException("Replay file changed after selection");
        } catch (IOException | RuntimeException error) { input.close(); inflater.end(); throw error; }
    }

    public ReplayFrame next() throws IOException {
        if (ended) return null;
        var frame = ReplayFormat.readFrame(input, inflater);
        if (frame == null) { ended = true; return null; }
        if (frame.millis < time || ++frames > ReplayFormat.MAX_FRAMES) throw new IOException("Invalid replay timeline");
        time = frame.millis;
        if (frame.kind == ReplayFrame.Kind.END) { ended = true; return null; }
        return frame;
    }

    @Override public void close() throws IOException { try { input.close(); } finally { inflater.end(); } }
}

package ru.he3hauka.toolvisuals.client.replay.io;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ReplayLibrary {
    private ReplayLibrary() {}

    public static ReplaySnapshot read(Path path) throws IOException {
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Replay is not a regular file");
        long length = Files.size(path);
        if (length < 64 || length > ReplayFormat.MAX_FILE_BYTES) throw new IOException("Invalid replay file size");
        ReplayHeader header;
        try (var input = new DataInputStream(Files.newInputStream(path))) { header = ReplayFormat.readHeader(input); }
        long duration;
        try (var channel = FileChannel.open(path, StandardOpenOption.READ)) {
            var bytes = ByteBuffer.allocate(ReplayFormat.FRAME_HEADER_BYTES);
            channel.position(length - bytes.capacity());
            while (bytes.hasRemaining()) if (channel.read(bytes) < 0) throw new IOException("Truncated replay footer");
            bytes.flip(); duration = ReplayFormat.footerDuration(bytes);
        }
        if (duration < 0) throw new IOException("Replay recording is not finalized");
        return new ReplaySnapshot(path, header, length, duration, 0, true);
    }

    public static List<ReplaySnapshot> list(Path directory) throws IOException {
        Files.createDirectories(directory);
        var results = new java.util.PriorityQueue<ReplaySnapshot>(Comparator.comparingLong(value -> value.header.createdMillis));
        try (var entries = Files.newDirectoryStream(directory, "*.tvr")) {
            int inspected = 0;
            for (var path : entries) {
                if (++inspected > 10000) break;
                try {
                    results.add(read(path));
                    if (results.size() > 128) results.remove();
                } catch (IOException | RuntimeException error) {ClientErrors.report("Read replay library entry "+path.getFileName(),error);}
            }
        }
        var sorted = new ArrayList<>(results); sorted.sort(Comparator.comparingLong((ReplaySnapshot value) -> value.header.createdMillis).reversed());
        return List.copyOf(sorted);
    }
}

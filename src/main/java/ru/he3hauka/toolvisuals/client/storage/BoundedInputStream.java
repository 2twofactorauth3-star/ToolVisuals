package ru.he3hauka.toolvisuals.client.storage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class BoundedInputStream extends FilterInputStream {
    private final long limit;
    private long consumed;
    private final boolean endAtLimit;

    public BoundedInputStream(InputStream input, long limit) {
        this(input, limit, false);
    }

    private BoundedInputStream(InputStream input, long limit, boolean endAtLimit) {
        super(Objects.requireNonNull(input));
        if (limit < 0 || limit == Long.MAX_VALUE) throw new IllegalArgumentException("Invalid input limit");
        this.limit = limit;
        this.endAtLimit = endAtLimit;
    }

    public static BoundedInputStream prefix(InputStream input, long length) { return new BoundedInputStream(input, length, true); }

    @Override public int read() throws IOException {
        if (endAtLimit && consumed == limit) return -1;
        int value = in.read();
        if (value != -1 && ++consumed > limit) throw new IOException("Input exceeds the size limit");
        return value;
    }

    @Override public int read(byte[] bytes, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        if (length == 0) return 0;
        if (endAtLimit && consumed == limit) return -1;
        int count = in.read(bytes, offset, (int) Math.min(length, Math.max(1, limit - consumed + (endAtLimit ? 0 : 1))));
        if (count > 0 && (consumed += count) > limit) throw new IOException("Input exceeds the size limit");
        return count;
    }

    @Override public long skip(long count) throws IOException {
        if (count <= 0) return 0;
        byte[] buffer = new byte[(int) Math.min(8192, count)];
        long skipped = 0;
        while (skipped < count) {
            int read = read(buffer, 0, (int) Math.min(buffer.length, count - skipped));
            if (read == -1) break;
            skipped += read;
        }
        return skipped;
    }

    @Override public boolean markSupported() { return false; }
    @Override public void mark(int readLimit) {}
    @Override public void reset() throws IOException { throw new IOException("Reset is not supported"); }
}

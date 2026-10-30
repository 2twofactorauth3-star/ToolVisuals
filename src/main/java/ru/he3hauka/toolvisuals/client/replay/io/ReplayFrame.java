package ru.he3hauka.toolvisuals.client.replay.io;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class ReplayFrame {
    public enum Kind {
        CONFIGURATION(1), PLAY(2), SELF(3), END(127);
        public final int id;
        Kind(int id) { this.id = id; }
        public static Kind byId(int id) {
            return switch (id) { case 1 -> CONFIGURATION; case 2 -> PLAY; case 3 -> SELF; case 127 -> END; default -> throw new IllegalArgumentException("Unknown replay frame kind"); };
        }
    }
    public final Kind kind;
    public final long millis;
    public final ByteBuffer payload;
    public final int size;

    private ReplayFrame(Kind kind, long millis, ByteBuffer payload) {
        this.kind = Objects.requireNonNull(kind);
        if (millis < 0 || millis > ReplayFormat.MAX_DURATION_MILLIS) throw new IllegalArgumentException("Invalid replay timestamp");
        if (payload.remaining() > ReplayFormat.MAX_PACKET_BYTES || (kind == Kind.END) != !payload.hasRemaining())
            throw new IllegalArgumentException("Invalid replay payload size");
        this.millis = millis; this.payload = payload.asReadOnlyBuffer(); size = payload.remaining();
    }

    public static ReplayFrame takeOwnership(Kind kind, long millis, byte[] payload) { return new ReplayFrame(kind, millis, ByteBuffer.wrap(payload)); }
    public static ReplayFrame copy(Kind kind, long millis, byte[] payload) { return takeOwnership(kind, millis, payload.clone()); }
}

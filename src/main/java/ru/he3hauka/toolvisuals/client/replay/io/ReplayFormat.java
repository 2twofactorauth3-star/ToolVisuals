package ru.he3hauka.toolvisuals.client.replay.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class ReplayFormat {
    public static final long MAGIC = 0x54565245504c4159L;
    public static final int VERSION = 1;
    public static final int MAX_PACKET_BYTES = 8 * 1024 * 1024;
    public static final long MAX_FILE_BYTES = 2L * 1024 * 1024 * 1024;
    public static final long MAX_DURATION_MILLIS = 86400000;
    public static final long MAX_FRAMES = 8000000;
    public static final int FRAME_HEADER_BYTES = 21;

    private ReplayFormat() {}

    public static void writeHeader(DataOutput output, ReplayHeader header) throws IOException {
        output.writeLong(MAGIC); output.writeInt(VERSION);
        uuid(output, header.recording); uuid(output, header.player);
        output.writeUTF(header.playerName); output.writeUTF(header.gameVersion);
        output.writeInt(header.protocol); output.writeLong(header.createdMillis);
    }

    public static ReplayHeader readHeader(DataInput input) throws IOException {
        if (input.readLong() != MAGIC || input.readInt() != VERSION) throw new IOException("Unsupported ToolVisuals replay format");
        try { return new ReplayHeader(uuid(input), uuid(input), input.readUTF(), input.readUTF(), input.readInt(), input.readLong()); }
        catch (IllegalArgumentException error) { throw new IOException("Invalid replay header", error); }
    }

    private static void uuid(DataOutput output, UUID id) throws IOException { output.writeLong(id.getMostSignificantBits()); output.writeLong(id.getLeastSignificantBits()); }
    private static UUID uuid(DataInput input) throws IOException { return new UUID(input.readLong(), input.readLong()); }

    public static int writeFrame(DataOutput output, ReplayFrame frame, long millis, Deflater deflater) throws IOException {
        byte[] raw = new byte[frame.size]; frame.payload.duplicate().clear().get(raw);
        byte[] stored = raw;
        if (raw.length >= 256) {
            var buffer = new byte[raw.length];
            deflater.reset(); deflater.setInput(raw); deflater.finish();
            int length = deflater.deflate(buffer);
            if (deflater.finished() && length + 16 < raw.length) stored = java.util.Arrays.copyOf(buffer, length);
        }
        var crc = new CRC32(); crc.update(raw);
        output.writeByte(frame.kind.id | (stored == raw ? 0 : 128));
        output.writeLong(millis); output.writeInt(raw.length); output.writeInt(stored.length); output.writeInt((int)crc.getValue()); output.write(stored);
        return FRAME_HEADER_BYTES + stored.length;
    }

    public static ReplayFrame readFrame(DataInputStream input, Inflater inflater) throws IOException {
        int tag = input.read();
        if (tag == -1) return null;
        long millis = input.readLong(); int size = input.readInt(), storedSize = input.readInt(), checksum = input.readInt();
        ReplayFrame.Kind kind;
        try { kind = ReplayFrame.Kind.byId(tag & 127); }
        catch (IllegalArgumentException error) { throw new IOException("Unknown replay frame", error); }
        boolean compressed = (tag & 128) != 0;
        if (size < 0 || size > MAX_PACKET_BYTES || storedSize < 0 || storedSize > size
                || (!compressed && size != storedSize) || (compressed && (size == 0 || storedSize == 0))
                || (kind == ReplayFrame.Kind.END) != (size == 0) || millis < 0 || millis > MAX_DURATION_MILLIS)
            throw new IOException("Replay frame exceeds format limits");
        byte[] stored = input.readNBytes(storedSize);
        if (stored.length != storedSize) throw new EOFException("Truncated replay frame");
        byte[] raw = stored;
        if (compressed) {
            raw = new byte[size]; inflater.reset(); inflater.setInput(stored);
            int offset = 0;
            try {
                while (offset < size && !inflater.finished()) {
                    int count = inflater.inflate(raw, offset, size - offset);
                    if (count == 0) throw new IOException("Invalid compressed replay frame");
                    offset += count;
                }
            } catch (DataFormatException error) { throw new IOException("Invalid compressed replay frame", error); }
            if (offset != size || !inflater.finished() || inflater.getRemaining() != 0) throw new IOException("Replay decompression exceeds the declared frame");
        }
        var crc = new CRC32(); crc.update(raw);
        if ((int)crc.getValue() != checksum) throw new IOException("Replay frame checksum mismatch");
        return ReplayFrame.takeOwnership(kind, millis, raw);
    }

    public static long footerDuration(ByteBuffer bytes) {
        if (bytes.remaining() != FRAME_HEADER_BYTES || Byte.toUnsignedInt(bytes.get()) != ReplayFrame.Kind.END.id) return -1;
        long time = bytes.getLong();
        return time >= 0 && time <= MAX_DURATION_MILLIS && bytes.getInt() == 0 && bytes.getInt() == 0 && bytes.getInt() == 0 ? time : -1;
    }
}
